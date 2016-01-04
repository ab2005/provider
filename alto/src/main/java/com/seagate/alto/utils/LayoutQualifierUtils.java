// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.utils;

// add a class header comment here

import android.content.Context;
import android.content.res.Configuration;

public class LayoutQualifierUtils {

    // this routine takes the qualifier string and evaluates to see if this
    // device in this orientation is a match

    // TODO: support p values not just dp, also support h

    public static boolean isQualified(Context context, String qualifier) {

        if (qualifier.startsWith("sw")) {
            // it doesn't matter if the qualifier is always true
            // or always not true, it only matters if it changes
            return true;
        } else if (qualifier.startsWith("w") && qualifier.endsWith("dp")) {

            // parse out the number and check it

            String swidth = qualifier.substring(1); // remove w
            swidth = swidth.substring(0, swidth.length() - 2); // remove dp
            int width = Integer.parseInt(swidth);

            Configuration configuration = context.getResources().getConfiguration();
            int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.

            return screenWidthDp > width;

        }

//        else if (qualifier.startsWith("h") && qualifier.endsWith("dp")) {
//
//        }


        return false;
    }


}
