// copyright (c) 2015 Seagate

package com.seagate.alto;

import android.os.Bundle;
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
import com.seagate.alto.utils.LayoutQualifierUtils;
import com.seagate.alto.utils.LogUtils;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements IFragmentStackHolder, NavigationView.OnNavigationItemSelectedListener {

    private static String TAG = LogUtils.makeTag(MainActivity.class);

//    private FragmentStack mFragmentStack = new FragmentStack();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // to enable cross-frag transitions
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        super.onCreate(savedInstanceState);

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

        // if the savedInstanceState is null, we are being called for the first time
        // otherwise the fragment stack will be restored to previous state magically

        if (savedInstanceState == null) {
            setFragment(new ListDetailFragment());
            navigationView.setCheckedItem(R.id.list);
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

            // when there are 2 master/detail items on the stack
            // and they are both showing the master/detail view
            // we want to pop both off at the same time to avoid looking like an error

            // if the names are the same and screen configuration is true, pop matching items off the stack

            // get the current entry backstack name and the previous entry backstack name
            // compare their 2 string
            // decide to pop 1 or 2 off stack

            boolean popTwo = false;

            FragmentManager fragmentManager = getSupportFragmentManager();
            int fragCount = fragmentManager.getBackStackEntryCount();

            if (fragCount >= 2) {

                String top = fragmentManager.getBackStackEntryAt(fragCount-1).getName();
                String next = fragmentManager.getBackStackEntryAt(fragCount-2).getName();

                if (top != null && top.equalsIgnoreCase(next)) {

                    // the name is in two parts -- label:qualifier
                    int colon = top.indexOf(":");
                    String qualifier = top.substring(colon + 1, top.length());
                    popTwo = LayoutQualifierUtils.isQualified(this, qualifier);
                }
            }

            if (popTwo) {
                fragmentManager.popBackStackImmediate();
            }

            super.onBackPressed(); // this pops the other one
//            mFragmentStack.pop();

            // finish if the stack is empty
            if (fragmentManager.getBackStackEntryCount() == 0) {
                finish();
            }

        }
    }

    private void logTheBackStack() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        int fragCount = fragmentManager.getBackStackEntryCount();
        Log.d(TAG, "backstack: -------- ");
        Log.d(TAG, "backstack: count = " + fragCount);

        for (int i = 0; i < fragCount; i++) {
            FragmentManager.BackStackEntry fbe = fragmentManager.getBackStackEntryAt(i);
            Log.d(TAG, "backstack: entry = " + i + " value = " + fbe.getName());
        }
        Log.d(TAG, "backstack: -------- ");
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
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // clear the back stack
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        transaction.replace(R.id.container, frag);
        String backStackName = null;
        if (frag instanceof IBackStackName) {
            backStackName = ((IBackStackName) frag).getBackStackName();
        }
        transaction.addToBackStack(backStackName);
        transaction.commit();

//        FragmentStack.FragmentEntry fe = new FragmentStack.FragmentEntry(frag.getClass(), null);
//        mFragmentStack.set(fe);

    }

    // the eltrans parameter is a list of hints for cool transitions
    public void pushFragment(Fragment frag, ArrayList<Pair<View, String>> eltrans) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.replace(R.id.container, frag);
        String backStackName = null;
        if (frag instanceof IBackStackName) {
            backStackName = ((IBackStackName) frag).getBackStackName();
        }
        transaction.addToBackStack(backStackName);
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

//        FragmentStack.FragmentEntry fe = new FragmentStack.FragmentEntry(frag.getClass(), null);
//        mFragmentStack.push(fe);

    }
}
