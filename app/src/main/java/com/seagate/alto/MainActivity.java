package com.seagate.alto;

import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.util.Pair;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.seagate.alto.events.BusMaster;
import com.seagate.alto.events.FragmentPushEvent;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IFragmentStackHolder, NavigationView.OnNavigationItemSelectedListener {

    private static String TAG = LogUtils.makeTag(MainActivity.class);

    private FragmentStack mFragmentStack = new FragmentStack();

    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // to enable cross-frag transitions
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        super.onCreate(savedInstanceState);

        mHandler = new Handler();

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        startFresco();

//        FragmentStack fs = restoreFragmentStack();
//        if (fs != null) {
//            fs.log();
//        }

        // if the savedInstanceState is null, we are being called for the first time
        // otherwise the fragment stack will be restored to previous state magically

        if (savedInstanceState == null) {

            setFragment(new CardDetailFragment());
            navigationView.setCheckedItem(R.id.card);
//            setFragment(new TileDetailFragment());
//            navigationView.setCheckedItem(R.id.tile);
//            setFragment(new ListDetailFragment());
//            navigationView.setCheckedItem(R.id.list);
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                logTheBackStack();
            }
        });

        BusMaster.getBus().register(this);

    }

    private void startFresco() {
//        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder()
//                .setBaseDirectoryName("SeagateCloud/ImageCache")
//                .setBaseDirectoryPath(getExternalFilesDir(Environment.DIRECTORY_PICTURES))
//                .setMaxCacheSize(50000000)
//                .build();
//        DiskCacheConfig smallImageDiskCacheConfig = DiskCacheConfig.newBuilder()
//                .setBaseDirectoryName("SeagateCloud/SmallImageCache")
//                .setBaseDirectoryPath(getExternalFilesDir(Environment.DIRECTORY_PICTURES))
//                .setMaxCacheSize(10000000)
//                .build();
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
//                .setMainDiskCacheConfig(diskCacheConfig)
//                .setSmallImageDiskCacheConfig(smallImageDiskCacheConfig)
                .build();
        Fresco.initialize(this, config);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

            // log the back stack

            logTheBackStack();

            super.onBackPressed();
//            mFragmentStack.pop();
        }
    }

    private void logTheBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int fragCount = fragmentManager.getBackStackEntryCount();
        Log.d(TAG, "backstack: -------- " + fragCount);
        Log.d(TAG, "backstack: count = " + fragCount);

        for (int i = 0; i < fragCount; i++) {
            FragmentManager.BackStackEntry fbe = fragmentManager.getBackStackEntryAt(i);
            Log.d(TAG, "backstack: entry = " + i + "value = " + fbe.getName());
        }
        Log.d(TAG, "backstack: -------- " + fragCount);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.list) {
            setFragment(new ListDetailFragment());
        } else if (id == R.id.card) {
            setFragment(new CardDetailFragment());
        } else if (id == R.id.tile) {
            setFragment(new TileDetailFragment());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void setFragment(Fragment frag) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, frag)
                .commit();

//        logTheBackStack();

//        FragmentStack.FragmentEntry fe = new FragmentStack.FragmentEntry(frag.getClass(), null);
//        mFragmentStack.set(fe);

    }

    // this event bus function let's you push a fragment from anywhere in the app
    @Subscribe
    public void answerAvailable(final FragmentPushEvent fpe) {
        // TODO: React to the event somehow!
        Log.d(TAG, "fragment pushed: " + fpe.getFragment().getClass().getSimpleName());

        pushFragment(fpe.getFragment(), fpe.getTransitions());

//
//        mHandler.post(new Runnable() {
//            @Override
//            public void run() {
//                pushFragment(fpe.getFragment(), fpe.getTransitions());
//            }
//        });

    }

//     bug workaround
//     http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: ");

//        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);

    }


    // the eltrans parameter is a list of hints for cool transitions
    public void pushFragment(Fragment frag, ArrayList<Pair<View, String>> eltrans) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.container, frag);
        transaction.addToBackStack(null);
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (eltrans != null) {
            for (Pair<View, String> et : eltrans) {
                transaction.addSharedElement(et.first, et.second);
            }
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
////            frag.setSharedElementEnterTransition(new ChangeBounds());
////            frag.setSharedElementReturnTransition(new ChangeBounds());
//            frag.setAllowEnterTransitionOverlap(true);
//            frag.setAllowReturnTransitionOverlap(true);
//        }

        transaction.commit();

//        transaction.commitAllowingStateLoss();

//        logTheBackStack();

//        FragmentStack.FragmentEntry fe = new FragmentStack.FragmentEntry(frag.getClass(), null);
//        mFragmentStack.push(fe);

    }
}
