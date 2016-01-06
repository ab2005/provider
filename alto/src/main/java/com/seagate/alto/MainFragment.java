// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

import android.graphics.Color;
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
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.seagate.alto.events.BusMaster;
import com.seagate.alto.utils.LayoutQualifierUtils;
import com.seagate.alto.utils.LogUtils;

import java.util.ArrayList;

public class MainFragment extends Fragment implements IBackPressHandler, IFragmentStackHolder, NavigationView.OnNavigationItemSelectedListener {

    private static String TAG = LogUtils.makeTag(MainFragment.class);

    private ActionBarDrawerToggle mToggle;
    private MaterialMenuDrawable materialMenu;
    private FragmentManager mFragmentManager;

    private View v;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // to enable cross-frag transitions
        // getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

//         super.onCreate(savedInstanceState);

        super.onCreateView(inflater, container, savedInstanceState);

        // setContentView(R.layout.activity_main);

        v = inflater.inflate(R.layout.fragment_main, container, false);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar);

        if (getActivity() instanceof AppCompatActivity) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        }

//        toolbar.setTitle(R.string.app_name);

        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        final DrawerLayout drawer = (DrawerLayout) v.findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(
                getActivity(), drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(mToggle);
        mToggle.syncState();

        materialMenu = new MaterialMenuDrawable(getContext(), Color.WHITE, MaterialMenuDrawable.Stroke.THIN);
        toolbar.setNavigationIcon(materialMenu);

        final NavigationView navigationView = (NavigationView) v.findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isFragmentStackAtBottom()) {
                    drawer.openDrawer(navigationView);
                } else {
                    onBackPressed();
                }

            }
        });

        mFragmentManager = getChildFragmentManager();

        // if the savedInstanceState is null, we are being called for the first time
        // otherwise the fragment stack will be restored to previous state magically

        if (savedInstanceState == null) {
            setFragment(new PagerFragment());
            navigationView.setCheckedItem(R.id.digest);
        } else {
            // the fragment stack is transferred over during rotation
            // but we need to set the home icon explicitly
            setupHomeIcon();
        }

        // should we show a back arrow or a hamburger

        // FragmentManager mFragmentManager = getChildFragmentManager();
        mFragmentManager.addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                setupHomeIcon();
                logTheBackStack();
            }
        });

        BusMaster.getBus().register(this);

        return v;
    }


    private void setupHomeIcon() {
        if (isFragmentStackAtBottom()) {
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.BURGER);
        } else {
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
        }
    }

    private boolean isFragmentStackAtBottom() {

        int fragCount = mFragmentManager.getBackStackEntryCount();

        if (fragCount <= 1) return true;

        if (fragCount > 2) return false; // we should check if more than 2 screens are equivalent

        if (fragCount == 2) {
            return topTwoEqual(mFragmentManager, fragCount);
        }

        return false;
    }

    private boolean topTwoEqual(FragmentManager mFragmentManager, int fragCount) {

        if (fragCount < 2) return false;

        String top = mFragmentManager.getBackStackEntryAt(fragCount-1).getName();
        String next = mFragmentManager.getBackStackEntryAt(fragCount - 2).getName();

        if (top != null && top.equalsIgnoreCase(next)) {

            // the name is in two parts -- label:qualifier
            int colon = top.indexOf(":");
            String qualifier = top.substring(colon + 1, top.length());
            return LayoutQualifierUtils.isQualified(getContext(), qualifier);
        }

        return false;
    }

    public void onBackPressed() {

        // v is the drawerlayout and you cannot find yourself

        DrawerLayout drawer = (DrawerLayout) v; // .findViewById(R.id.drawer_layout);
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

            int fragCount = mFragmentManager.getBackStackEntryCount();

            popTwo = topTwoEqual(mFragmentManager, fragCount);

            if (popTwo) {
                mFragmentManager.popBackStackImmediate();
            }

            mFragmentManager.popBackStackImmediate();

            // finish if the stack is empty
            if (mFragmentManager.getBackStackEntryCount() == 0) {
                getActivity().finish();
            }
        }
    }

    private void logTheBackStack() {

        int fragCount = mFragmentManager.getBackStackEntryCount();
        Log.d(TAG, "backstack: -------- ");
        Log.d(TAG, "backstack: count = " + fragCount);

        for (int i = 0; i < fragCount; i++) {
            FragmentManager.BackStackEntry fbe = mFragmentManager.getBackStackEntryAt(i);
            Log.d(TAG, "backstack: entry = " + i + " value = " + fbe.getName());
        }
        Log.d(TAG, "backstack: -------- ");
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getActivity().getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }

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

        if (id == R.id.digest) {
            setFragment(new PagerFragment());
        } else if (id == R.id.tags) {
            setFragment(new ListDetailFragment());
        } else if (id == R.id.just_added) {
            setFragment(new TileDetailFragment());
        } else if (id == R.id.favorites) {
            setFragment(new CardDetailFragment());
        } else {
            setFragment(new ListDetailFragment());
        }

//        DrawerLayout drawer = (DrawerLayout) v.findViewById(R.id.drawer_layout);
//        drawer.closeDrawer(GravityCompat.START);

        ((DrawerLayout) v).closeDrawer(GravityCompat.START);

        return true;
    }

    private void setFragment(Fragment frag) {

        FragmentTransaction transaction = mFragmentManager.beginTransaction();

        // clear the back stack
        mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        transaction.replace(R.id.container, frag);
        String backStackName = null;
        if (frag instanceof IBackStackName) {
            backStackName = ((IBackStackName) frag).getBackStackName();
        }
        transaction.addToBackStack(backStackName);
        transaction.commit();

    }

    // the eltrans parameter is a list of hints for cool transitions
    public void pushFragment(Fragment frag, ArrayList<Pair<View, String>> eltrans) {

        FragmentTransaction transaction = mFragmentManager.beginTransaction();

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

    }
}
