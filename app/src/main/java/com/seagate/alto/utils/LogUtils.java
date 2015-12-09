package com.seagate.alto.utils;

// add a class header comment here

public class LogUtils {

    public static final String TAG_PREFIX = "alto-";

    public static String makeTag(@SuppressWarnings("rawtypes") Class clazz) {
        String tag = clazz.getSimpleName();
        tag = TAG_PREFIX + tag;
        // limit tags to 23 characters
        if (tag.length() > 23)
            tag = tag.substring(0, 22);
        return tag;
    }
}
