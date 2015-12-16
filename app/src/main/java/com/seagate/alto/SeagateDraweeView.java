// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;

import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.view.SimpleDraweeView;

public class SeagateDraweeView extends SimpleDraweeView {
    public SeagateDraweeView(Context context) {
        super(context);
    }

    public SeagateDraweeView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeagateDraweeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SeagateDraweeView(Context context, GenericDraweeHierarchy hierarchy) {
        super(context, hierarchy);
    }

    // Silly hack to fix Activity transition support.  Without this method, the transitions
    // cause the DraweeView to disappear.
    public void animateTransform(Matrix matrix) {
        invalidate();
    }

}
