package com.seagate.alto.events;

// event for adding fragment to the stack

import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.View;

import java.util.ArrayList;

public class FragmentPushEvent {

    private Fragment mFragment;
    private ArrayList<Pair<View, String>> mTransitions;

    public ArrayList<Pair<View, String>> getTransitions() {
        return mTransitions;
    }

    public void setTransitions(ArrayList<Pair<View, String>> mTransitions) {
        this.mTransitions = mTransitions;
    }

    public Fragment getFragment() {
        return mFragment;
    }

    public void setFragment(Fragment mFragment) {
        this.mFragment = mFragment;
    }


}
