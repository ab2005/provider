// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

// display a full size photo

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.android.ZoomableDraweeView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ProgressBarDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.seagate.alto.utils.LogUtils;

public class PhotoFragment extends Fragment {

    private final static String TAG = LogUtils.makeTag(PhotoFragment.class);

    private Adapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View v = inflater.inflate(R.layout.photo_pager_fragment, container, false);

        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

//        TabLayout tabs = (TabLayout) v.findViewById(R.id.tabs);
//        tabs.setupWithViewPager(viewPager);

        return v;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
        if (getParentFragment() instanceof IToolbarHolder) {
            ((IToolbarHolder) getParentFragment()).hideToolBar();
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        if (getParentFragment() instanceof IToolbarHolder) {
            ((IToolbarHolder) getParentFragment()).showToolBar();
        }
    }

    private void setupViewPager(ViewPager viewPager) {

        int index = 0;
        Bundle args = getArguments();
        if (args != null) {
            index = args.getInt(PlaceholderContent.INDEX);
        }


        // if you ARE a fragment and you HAVE fragments, use the ChildFragmentManager
        mAdapter = new Adapter(this.getChildFragmentManager());
//        mAdapter.addFragment(new ListDetailFragment(), "List");
//        mAdapter.addFragment(new TileDetailFragment(), "Tile");
//        mAdapter.addFragment(new CardDetailFragment(), "Card");
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(index);
    }

//    @Override
//    public String getBackStackName() {
//        return "pager-fragment";
//    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d(TAG, "onResume");
//    }

    static class Adapter extends FragmentPagerAdapter {
//        private final List<Fragment> mFragmentList = new ArrayList<>();
//        private final List<String> mFragmentTitleList = new ArrayList<>();

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
//            return mFragmentList.get(position);

            PhotoSubFragment pf = new PhotoSubFragment();
            Bundle args = new Bundle();
            args.putInt(PlaceholderContent.INDEX, position);
            pf.setArguments(args);

            return pf;
        }

        @Override
        public int getCount() {
//            return mFragmentList.size();
            return PlaceholderContent.getCount();
        }

//        public void addFragment(Fragment fragment, String title) {
//            mFragmentList.add(fragment);
//            mFragmentTitleList.add(title);
//        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Weenie";
        }
    }

    // individual photo fragments

    public static class PhotoSubFragment extends Fragment {

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

            View v = inflater.inflate(R.layout.photo_fragment, container, false);

            int index = 0;
            Bundle args = getArguments();
            if (args != null) {
                index = args.getInt(PlaceholderContent.INDEX);
            }

            ZoomableDraweeView zdv = (ZoomableDraweeView) v.findViewById(R.id.image);
            if (zdv != null) {
                zdv.setController(
                        Fresco.newDraweeControllerBuilder()
                                .setUri(PlaceholderContent.getUri(index))
                                .build());

                GenericDraweeHierarchy hierarchy =
                        new GenericDraweeHierarchyBuilder(container.getResources())
                                .setActualImageScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                                .setProgressBarImage(new ProgressBarDrawable())
                                .build();

                zdv.setHierarchy(hierarchy);

            }

            return v;
        }
    }

}
