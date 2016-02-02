// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.seagate.alto.PlaceholderContent;
import com.seagate.alto.utils.LayoutUtils;
import com.seagate.alto.utils.LogUtils;
import com.seagate.alto.utils.ScreenUtils;

import java.util.ArrayList;

public class DigestCellView extends RelativeLayout {

    private final static String TAG = LogUtils.makeTag(DigestCellView.class);

    private static final int MAX_IMAGES_IN_CELL = 5;

    // content
    private long mDigestId;
    private int mImagePanelCount;
    private int mInfoPanelCount = 1;
    private int mPosition;
    private Cursor mContentCursor;
    private int mContentCursorCount = 0;
    private long mTimestamp;

    // layout
    private Rect mLayoutBounds;
    private ArrayList<Rect> mChildrenBounds;
    private Rect mInfoBounds;
    private DigestCellLayout mDigestCellLayout;

    private ImageSwitchView[] mImageSwitchViews;
    private InfoPanelView mInfoPanelView;

    private int mCellBorder;
    private int mPanelPadding;

    public DigestCellView(Context context) {
        super(context);
        init(context);
    }

    public DigestCellView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context);
    }

    public DigestCellView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        Log.d(TAG, "init()" + this);

        mImageSwitchViews = new ImageSwitchView[MAX_IMAGES_IN_CELL];
        for (int i = 0; i < MAX_IMAGES_IN_CELL; i++) {
            ImageSwitchView isw = new ImageSwitchView(context);
            this.addView(isw);
            mImageSwitchViews[i] = isw;
        }

        mInfoPanelView = new InfoPanelView(context);
        this.addView(mInfoPanelView);

        mCellBorder = LayoutUtils.getBorderSize(getWidth());
        mPanelPadding = LayoutUtils.getPanelPadding(getWidth());
    }


    public void loadContent(int position) {
        // load content only when viewHolder is bind. not gonna be called when you rotate the screen.
        Log.d(TAG, "loadContent()");

        synchronized (this) {
            mPosition = position;
            // TODO: 1/14/16  mImagePanelCount = getImagePanelCount(position);
            mImagePanelCount = mPosition % MAX_IMAGES_IN_CELL + 1;
            mDigestCellLayout = DigestCellLayouts.getLayoutForSize(mImagePanelCount, mPosition);        // not gonna change when orientation changed.

            // set drawee source
            setMultiDraweeSource(mPosition);
            // set date info
            setInfoPanel(mPosition);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // gonna be called when rotating the screen. Recalculate the layouts/bounds/sizes here.
        Log.d(TAG, "onMeasure()");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size;
        int realSize;

        DigestRecyclerView parent = (DigestRecyclerView) getParent();
        size = ScreenUtils.isPortrait() ? parent.getWidth(): parent.getHeight();
        int margin = LayoutUtils.getDigestMargin(size);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        lp.leftMargin = margin;
        lp.topMargin = margin;
        lp.rightMargin = ScreenUtils.isPortrait()? margin : 0;
        lp.bottomMargin = ScreenUtils.isPortrait()? 0 : margin;
        setLayoutParams(lp);

        realSize = size - 2 * margin;
        setMeasuredDimension(realSize, realSize);

        mLayoutBounds = new Rect(0, 0, realSize, realSize);
        mLayoutBounds.inset(mCellBorder, mCellBorder);

        mChildrenBounds = new ArrayList<>();
        for (int i = 0; i < mImagePanelCount; i++) {
            Rect childBounds = mDigestCellLayout.getPanelRect(i, mLayoutBounds);
            childBounds.inset(mPanelPadding, mPanelPadding);
            mChildrenBounds.add(childBounds);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        synchronized (this) {
            for (int i = 0; i < MAX_IMAGES_IN_CELL; i++) {
                if (i < mImagePanelCount) {
                    mImageSwitchViews[i].setVisibility(VISIBLE);
                    mImageSwitchViews[i].setBounds(mChildrenBounds.get(i));
                } else {
                    mImageSwitchViews[i].setVisibility(GONE);
                }
            }
            mInfoBounds = mDigestCellLayout.getInfoRect(mLayoutBounds);
            mInfoBounds.inset(mPanelPadding, mPanelPadding);
            mInfoPanelView.setBounds(mInfoBounds);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw()");
        super.onDraw(canvas);
    }

    private void setMultiDraweeSource(int position) {
        Log.d(TAG, "setMultiDraweeSource()");

        synchronized (this) {
            Log.d(TAG, "mPanelCount = " + mImagePanelCount);
            for (int i = 0; i < mImagePanelCount; i++) {
                // have to build a new DraweeController if you want to set new URI.
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(PlaceholderContent.getThumbnailUri(((i + 1) * (position + 1))))
                        .build();
                mImageSwitchViews[i].loadContent(controller);
                Log.d(TAG, "Current drawable of imageSwitchView(" + i + "): " + mImageSwitchViews[i].getCurrentDrawable());
            }
        }

    }

    public void resumeView() {
        Log.d(TAG, "resumeView()");
        synchronized (this) {
            for (ImageSwitchView isv : mImageSwitchViews) {
                isv.resumeSwitching();
            }
        }

    }

    public void pauseView() {
        Log.d(TAG, "pauseView()");
        synchronized (this) {
            for (ImageSwitchView isv : mImageSwitchViews) {
                isv.pauseSwitching();
            }
        }
    }

    private void setInfoPanel(int position) {
        mInfoPanelView.setPosition(position);
//        long offset = Timestamp.valueOf("2015-01-01 00:00:00").getTime();
//        long end = Timestamp.valueOf("2016-01-01 00:00:00").getTime();
//        long diff = end - offset + 1;
//        mTimestamp = offset + (long)(diff * (position + 1) / 100);      // TODO: 1/14/16 getTimeStamp here
        mTimestamp = PlaceholderContent.getTimestamp(position);

        mInfoPanelView.setTimestamp(mTimestamp);

        mInfoPanelView.setHasInfo(true);
    }

}
