package com.seagate.alto.utils;

public class LayoutUtils {
    private static final float PANEL_PADDING_FACTOR = 0.005f;
    private static final float COLLAGE_BORDER_FACTOR = 0.005f;
    private static final int MINIMUM_ULTRA_LIGHT_SIZE = 640;
    private static int sActionBarHeight;

    public static int getNumGridCols() {
        return ScreenUtils.isPortrait() ? 4 : 7;
    }

    public static int getPageMargin() {
        int pageMargin;
        ScreenUtils.AspectRatio aspectRatio = ScreenUtils.getAspectRatio();
        switch (aspectRatio) {
            case Ratio16x9:
                pageMargin = 50;
                break;
            default:
                pageMargin = 25;
                break;
        }
        return pageMargin;
    }

    public static int getPanelPadding(int width) {
        return Math.round(Math.max(2, width * PANEL_PADDING_FACTOR));
    }

    public static int getBorderSize(int width) {
        return Math.round(Math.max(2, width * COLLAGE_BORDER_FACTOR));
    }

    public static int getActionBarHeight() {
        return sActionBarHeight;
    }

}