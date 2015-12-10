//  copyright (c) 2015. seagate technology plc. all rights reserved.

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
