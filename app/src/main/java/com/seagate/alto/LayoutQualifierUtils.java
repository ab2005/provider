package com.seagate.alto;

// add a class header comment here

import android.content.Context;
import android.content.res.Configuration;

public class LayoutQualifierUtils {

    // this routine takes the qualifier string and evaluates to see if it is true
    // I wanted to find this code in AOSP to make sure it matches

    // todo: support p values not just dp

    public static boolean isMatch(Context context, String qualifier) {

        if (qualifier.startsWith("sw")) {
            return true;
        } else if (qualifier.startsWith("w") && qualifier.endsWith("dp")) {

            // parse out the number and check it

            String swidth = qualifier.substring(1); // remove w
            swidth = swidth.substring(0, swidth.length() - 2); // remove dp
            int width = Integer.parseInt(swidth);

            Configuration configuration = context.getResources().getConfiguration();
            int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.

            return screenWidthDp > width;

        } else if (qualifier.startsWith("h") && qualifier.endsWith("dp")) {

            // parse out the number and check it

            String swidth = qualifier.substring(1); // remove h
            swidth = swidth.substring(0, swidth.length() - 1); // remove dp




        }


        return false;
    }


}
