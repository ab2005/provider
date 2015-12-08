// copyright (c) 2015 Seagate

package com.seagate.alto;

import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.View;

import java.util.ArrayList;


public interface IFragmentStackHolder {
    void pushFragment(Fragment frag, ArrayList<Pair<View, String>> eltrans);
}
