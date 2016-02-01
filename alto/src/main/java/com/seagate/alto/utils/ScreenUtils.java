// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

public class ScreenUtils {

    private static ScreenUtils sMe;
    private static final DisplayMetrics sMetrics = new DisplayMetrics();
    private static Context sContext;
    private static int sOrientation;

    public static ScreenUtils getInstance() {
        if (sMe == null) {
            sMe = new ScreenUtils();
        }

        return sMe;
    }

    public static void init(Context context) {
        getInstance();
        sContext = context;
        sOrientation = getOrientation();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        display.getMetrics(sMetrics);
    }

    public static DisplayMetrics getMetrics() {
        if (sOrientation != getOrientation()) {
            sOrientation = getOrientation();
            WindowManager wm = (WindowManager) sContext.getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            display.getMetrics(sMetrics);
        }

        return sMetrics;
    }

    public static int getWidthInPixels() {
        return getMetrics().widthPixels;
    }

    public static int getHeightInPixels() {
        return getMetrics().heightPixels;
    }

    public static int getOrientation() {
        return sContext != null ? sContext.getResources().getConfiguration().orientation : 0;
    }

    public static boolean isPortrait() {
        return ScreenUtils.getOrientation() == Configuration.ORIENTATION_PORTRAIT;
    }

}
