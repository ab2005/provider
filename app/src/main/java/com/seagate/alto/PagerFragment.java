// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.seagate.alto.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

// Fragment that holds a Viewpager with Tabs

public class PagerFragment extends Fragment implements IBackStackName {

    private final static String TAG = LogUtils.makeTag(PagerFragment.class);

    private Adapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View v = inflater.inflate(R.layout.pager_fragment, container, false);

        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabs = (TabLayout) v.findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        return v;
    }

    // Add Fragments to Tabs
    private void setupViewPager(ViewPager viewPager) {
        // if you ARE a fragment and you HAVE fragments, use the ChildFragmentManager
        mAdapter = new Adapter(this.getChildFragmentManager());
        mAdapter.addFragment(new ListDetailFragment(), "List");
        mAdapter.addFragment(new TileDetailFragment(), "Tile");
        mAdapter.addFragment(new CardDetailFragment(), "Card");
        viewPager.setAdapter(mAdapter);
    }

    @Override
    public String getBackStackName() {
        return "pager-fragment";
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");

//        mAdapter.notifyDataSetChanged();
    }

    static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

}
