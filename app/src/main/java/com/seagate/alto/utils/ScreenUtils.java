// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.utils;

import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

public class ScreenUtils {

    private static final String TAG = ScreenUtils.class.getName();
    public enum DpiName { LessThanHDPI, HDPI, XDPI, XXDPI, XXXDPI, MoreThanXXX }
    public enum AspectRatio {Ratio16x9, Ratio4x3, Ratio3x2}
    private static ScreenUtils sMe;
    private static final DisplayMetrics sMetrics = new DisplayMetrics();
    private static Context sContext;
    private static int sOrientation;
    private static int sMaxTextureSize = -1;

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

    public static int percent2PixelsX(float percentX) {
        float screenWidthInInches = pixels2InchesX(getWidthInPixels());
        float widthInInches = screenWidthInInches * percentX;
        return getInchesAsPixelsX(widthInInches);
    }

    public static int percent2PixelsY(float percentY) {
        float screenHeightInInches = pixels2InchesY(getHeightInPixels());
        float heightInInches = screenHeightInInches * percentY;
        return getInchesAsPixelsY(heightInInches);
    }

    /**
     * Will return 240, 320, 480, 640
     * @return
     */
    public static float densityDpi() {
        return getMetrics().densityDpi;
    }

    public static DpiName getDpiName() {
        int dpi = (int) densityDpi();
        int MARGIN = 5;
        if ((MARGIN + dpi - 240) < 0)
            return DpiName.LessThanHDPI;
        else if (Math.abs(dpi-240) < MARGIN)
            return DpiName.HDPI;
        else if (Math.abs(dpi-320) < MARGIN)
            return DpiName.XDPI;
        else if (Math.abs(dpi-480) < MARGIN)
            return DpiName.XXDPI;
        else if (Math.abs(dpi-640) < MARGIN)
            return DpiName.XXXDPI;
        else
            return DpiName.MoreThanXXX;
    }





    public static float pixels2InchesX(int x) {
        // densityDpi seems to give much more accurate results here than xdpi (tmyles)
        return (float)x / getMetrics().densityDpi;
    }

    public static float pixels2InchesY(int y) {
        // densityDpi seems to give much more accurate results here than ydpi (tmyles)
        return (float)y / getMetrics().densityDpi;
    }

    public static int getInchesAsPixelsX(float inches) {
        return Math.round(inches * getMetrics().xdpi);
    }

    public static int getInchesAsPixelsY(float inches) {
        return Math.round(inches * getMetrics().ydpi);
    }

    public static float dpFromPx(float px)
    {
        return px / sMetrics.density;
    }


    public static float pxFromDp(float dp)
    {
        return dp * sMetrics.density;
    }

    public static boolean isXHighOrBetterDisplay() {
        return ScreenUtils.densityDpi() >= DisplayMetrics.DENSITY_XHIGH;
    }

    /**
     *  Use height to convert to 16:9 aspect ratio
     */
    public static int get16x9Width(int height) {
        return Math.round(height * 1.8f);
    }

    /**
     *  Use width to convert to 16:9 aspect ratio
     */
    public static int get16x9Height(int width) {
        return Math.round(width * 0.6f);
    }

    public static int getOrientation() {
        return sContext != null ? sContext.getResources().getConfiguration().orientation : 0;
    }

    public static boolean isPortrait() {
        return ScreenUtils.getOrientation() == Configuration.ORIENTATION_PORTRAIT;
    }

    public static AspectRatio getAspectRatio() {
        int width = getWidthInPixels();
        int height = getHeightInPixels();

        float ratio = (float)Math.max(width, height) / Math.min(width, height);

        AspectRatio aspectRatio;
        if (ratio <= (float) 3/2)
            aspectRatio = AspectRatio.Ratio3x2;
        else if (ratio <= (float)4/3)
            aspectRatio = AspectRatio.Ratio4x3;
        else
            aspectRatio = AspectRatio.Ratio16x9;

        return aspectRatio;
    }

    public static float getScreenSize() {
        double size;

        getMetrics();
        // densityDpi seems to give much more accurate results here than
        // xdpi & ydpi  (tmyles)
        float screenWidth = sMetrics.widthPixels / sMetrics.densityDpi;
        float screenHeight = sMetrics.heightPixels / sMetrics.densityDpi;

        size = Math.sqrt(Math.pow(screenWidth, 2) + Math.pow(screenHeight, 2));

        return (float)size;
    }

    public static int getMaxTextureSize() {
        if (sMaxTextureSize == -1) {
            sMaxTextureSize = getMaximumTextureSize();
        }

        return sMaxTextureSize;
    }

    private static int getMaximumTextureSize() {
        EGL10 egl = (EGL10) EGLContext.getEGL();
        EGLDisplay display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

        // Initialise
        int[] version = new int[2];
        egl.eglInitialize(display, version);

        // Query total number of configurations
        int[] totalConfigurations = new int[1];
        egl.eglGetConfigs(display, null, 0, totalConfigurations);

        // Query actual list configurations
        EGLConfig[] configurationsList = new EGLConfig[totalConfigurations[0]];
        egl.eglGetConfigs(display, configurationsList, totalConfigurations[0], totalConfigurations);

        int[] textureSize = new int[1];
        int maximumTextureSize = 0;

        // Iterate through all the configurations to located the maximum texture size
        for (int i = 0; i < totalConfigurations[0]; i++) {
            // Only need to check for width since opengl textures are always squared
            egl.eglGetConfigAttrib(display, configurationsList[i], EGL10.EGL_MAX_PBUFFER_WIDTH, textureSize);

            // Keep track of the maximum texture size
            if (maximumTextureSize < textureSize[0]) {
                maximumTextureSize = textureSize[0];
            }
        }

        // Release
        egl.eglTerminate(display);
        Log.i("ZZOPENGL", "Maximum GL texture size: " + Integer.toString(maximumTextureSize));

        return maximumTextureSize;
    }
}
