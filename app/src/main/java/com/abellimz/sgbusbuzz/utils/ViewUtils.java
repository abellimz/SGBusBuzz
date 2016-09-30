package com.abellimz.sgbusbuzz.utils;

import android.content.res.Resources;

/**
 * Created by Abel on 9/19/2016.
 */

public class ViewUtils {

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

}
