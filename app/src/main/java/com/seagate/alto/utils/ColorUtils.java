package com.seagate.alto.utils;

import android.graphics.Color;

public class ColorUtils {
    public enum CompanyColor {Blue, Green, Purple, Yellow, Orange, Red}
    public static final int[] COMPANY_COLORS = {
            Color.rgb(9, 113, 206),     // Blue
            Color.rgb(182, 219, 4),     // Green
            Color.rgb(161, 50, 146),    // Purple
            Color.rgb(253, 213, 0),     // Yellow
            Color.rgb(247, 122, 1),     // Orange
            Color.rgb(240, 35, 40),     // Red
    };

    public static int getCompanyColor(CompanyColor color) {
        return COMPANY_COLORS[color.ordinal()];
    }

    public static int getCompanyColor(int index) {
        return COMPANY_COLORS[index % COMPANY_COLORS.length];
    }
}
