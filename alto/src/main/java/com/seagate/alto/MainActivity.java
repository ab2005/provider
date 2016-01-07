// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;

import com.seagate.alto.utils.LogUtils;

import java.util.List;

// this activity switches between the splash fragment and the main fragment

public class MainActivity extends AppCompatActivity implements IContentSwitcher {

    private static String TAG = LogUtils.makeTag(MainActivity.class);

    private FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // to enable cross-frag transitions
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.main);

        mFragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            setFragment(new SplashFragment());
        }
    }

    private void setFragment(Fragment frag) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.main_container, frag);
        transaction.commit();
    }

    @Override
    public void onBackPressed() {
        // send it to the last fragment -- there should be only one fragment in this fragment manager

        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();

        if (fragments != null && fragments.size() > 0) {
            Fragment currentFragment = fragments.get(fragments.size() - 1);

            if (currentFragment instanceof IBackPressHandler) {
                ((IBackPressHandler) currentFragment).onBackPressed();
            }
        }
    }

    @Override
    public void switchToMain() {
        Log.d(TAG, "switching to main");
        setFragment(new StackFragment());

    }
}
