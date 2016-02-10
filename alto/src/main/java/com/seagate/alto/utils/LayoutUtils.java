// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.utils;

public class LayoutUtils {
    private static final float DIGEST_MARGIN_FACTOR = 0.05f;
    private static final float PANEL_PADDING_FACTOR = 0.005f;
    private static final float DIGEST_BORDER_FACTOR = 0.01f;
    private static final int MINIMUM_ULTRA_LIGHT_SIZE = 640;

    public static int getPanelPadding(int width) {
        return Math.round(Math.max(2, width * PANEL_PADDING_FACTOR));
    }

    public static int getDigestMargin(int width) {
        return Math.round(Math.max(10, width * DIGEST_MARGIN_FACTOR));
    }

    public static int getBorderSize(int width) {
        return Math.round(Math.max(4, width * DIGEST_BORDER_FACTOR));
    }

}