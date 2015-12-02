// copyright (c) 2015 Seagate

package com.seagate.alto;

// add a class header comment here

public class DetailListFragment extends SplitFragment {

    protected String makeTag() {
        return LogUtils.makeTag(DetailListFragment.class);
    }

    protected int getLayout() {
        return R.layout.detail_split;
    }

}
