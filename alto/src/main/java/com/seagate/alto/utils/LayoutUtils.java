package com.seagate.alto.utils;

import android.content.res.TypedArray;

import com.seagate.alto.AltoApplication;

public class LayoutUtils {
    private static final float PANEL_PADDING_FACTOR = 0.005f;
    private static final float DIGEST_BORDER_FACTOR = 0.01f;
    private static final int MINIMUM_ULTRA_LIGHT_SIZE = 640;
    private static int sActionBarHeight;


    public static int getPanelPadding(int width) {
        return Math.round(Math.max(2, width * PANEL_PADDING_FACTOR));
    }

    public static int getBorderSize(int width) {
        return Math.round(Math.max(4, width * DIGEST_BORDER_FACTOR));
    }

    public static int getActionBarHeight() {
        final TypedArray styledAttributes = AltoApplication.getInstance().getTheme().obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
        sActionBarHeight = (int) styledAttributes.getDimension(0, 0);
        styledAttributes.recycle();
        return sActionBarHeight;
    }

    public static int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = AltoApplication.getInstance().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = AltoApplication.getInstance().getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}