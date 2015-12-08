package com.seagate.alto.events;

// add a class header comment here

import android.support.v4.util.Pair;
import android.view.View;

import java.util.ArrayList;

public class ItemSelectedEvent {

    private int mPosition;
    private ArrayList<Pair<View, String>> mPairs;

    public ArrayList<Pair<View, String>> getPairs() {
        return mPairs;
    }

    public void setPairs(ArrayList<Pair<View, String>> pairs) {
        mPairs = pairs;
    }


    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int mPosition) {
        this.mPosition = mPosition;
    }

    public ItemSelectedEvent(int position, ArrayList<Pair<View, String>> pairs) {
        mPosition = position;
        mPairs = pairs;
    }
}
