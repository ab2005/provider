// copyright (c) 2015 Seagate

package com.seagate.alto;

// add a class header comment here

import com.seagate.alto.utils.LogUtils;

public class DetailListFragment extends ListDetailFragment {

    protected String makeTag() {
        return LogUtils.makeTag(DetailListFragment.class);
    }

    protected int getLayout() {
        return R.layout.detail_split;
    }

}
