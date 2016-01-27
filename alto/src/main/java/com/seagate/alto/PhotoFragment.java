// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

// display a full screen photo in a view pager

import android.graphics.drawable.Animatable;
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

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.imagepipeline.image.ImageInfo;
import com.seagate.alto.utils.LogUtils;

import me.relex.photodraweeview.PhotoDraweeView;

public class PhotoFragment extends Fragment {

    private final static String TAG = LogUtils.makeTag(PhotoFragment.class);

    private Adapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        View v = inflater.inflate(R.layout.photo_pager_fragment, container, false);

        ViewPager viewPager = (ViewPager) v.findViewById(R.id.viewpager);
        setupViewPager(viewPager);

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
        viewPager.setAdapter(mAdapter);
        viewPager.setCurrentItem(index);
    }

    static class Adapter extends FragmentPagerAdapter {

        public Adapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            PhotoSubFragment pf = new PhotoSubFragment();
            Bundle args = new Bundle();
            args.putInt(PlaceholderContent.INDEX, position);
            pf.setArguments(args);

            return pf;
        }

        @Override
        public int getCount() {
            return PlaceholderContent.getCount();
        }

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

            final PhotoDraweeView photoDraweeView = (PhotoDraweeView) v.findViewById(R.id.image);
            PipelineDraweeControllerBuilder controller = Fresco.newDraweeControllerBuilder();
            controller.setUri(PlaceholderContent.getUri(index));
            controller.setOldController(photoDraweeView.getController());
            controller.setControllerListener(new BaseControllerListener<ImageInfo>() {
                @Override
                public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                    super.onFinalImageSet(id, imageInfo, animatable);
                    if (imageInfo == null) {
                        return;
                    }
                    photoDraweeView.update(imageInfo.getWidth(), imageInfo.getHeight());
                }
            });
            photoDraweeView.setController(controller.build());

            return v;
        }
    }

}
