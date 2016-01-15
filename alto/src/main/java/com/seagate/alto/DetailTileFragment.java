// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

// add a class header comment here

import com.seagate.alto.utils.LogUtils;

public class DetailTileFragment extends ListDetailFragment implements IBackStackName{

    private String TAG = makeTag();

    protected String makeTag() {
        return LogUtils.makeTag(DetailTileFragment.class);
    }

    protected int getLayout() {
        return R.layout.detail_tile;
    }

    @Override
    public String getBackStackName() {
        return "tile-detail:w600dp";
    }

}
