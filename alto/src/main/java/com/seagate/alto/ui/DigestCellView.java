// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.ui;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.seagate.alto.PlaceholderContent;
import com.seagate.alto.R;
import com.seagate.alto.utils.ColorUtils;
import com.seagate.alto.utils.LayoutUtils;
import com.seagate.alto.utils.LogUtils;
import com.seagate.alto.utils.ScreenUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DigestCellView extends View {

    private final static String TAG = LogUtils.makeTag(DigestCellView.class);

//    private MultiDraweeHolder<GenericDraweeHierarchy> mMultiDraweeHolder;
    private ArrayList<DraweeHolder> mMyMultiHolders;
    private final InfoPanel mInfoPanel = new InfoPanel();

    enum TransitionType {None, Crossfade, FlipHorizontal, FlipVertical}

    // TODO: 1/20/16 multiple handler
    private Handler transitionHandler = new Handler();

    private static final int MAX_IMAGES_IN_CELL = 5;
    private static final int ROTATE_FREQUENCY = 5000; // ms

    // content
    private long mDigestId;
    private int mImagePanelCount;
    private int mInforPanelCount = 1;
    private int mPosition;
    private Cursor mContentCursor;
    private int mContentCursorCount = 0;
    private long mTimestamp;

    // layout
    private Rect mLayoutBounds;
    private ArrayList<Rect> mChildrenBounds;
    private Rect mInfoBounds;
    private DigestCellLayout mDigestCellLayout;

    private int mCellBorder;
    private int mPanelPadding;

    public DigestCellView(Context context) {
        super(context);
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
        Log.d(TAG, "init()");

        mMyMultiHolders = new ArrayList<>();

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

            // drawee source
            setMultiDraweeSource(mPosition);

            // date info
            long offset = Timestamp.valueOf("2015-01-01 00:00:00").getTime();
            long end = Timestamp.valueOf("2016-01-01 00:00:00").getTime();
            long diff = end - offset + 1;
            mTimestamp = offset + (long)(Math.random() * diff);      // TODO: 1/14/16 getTimeStamp here
            mInfoPanel.setTimestamp(mTimestamp);

        }

    }


    @Override
    public void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow()");
        super.onDetachedFromWindow();
        detachDraweeHolders();

        transitionHandler.removeCallbacks(transitionRunnable);
    }

    @Override
    public void onStartTemporaryDetach() {
        Log.d(TAG, "onStartTemporaryDetach()");
        super.onStartTemporaryDetach();
        detachDraweeHolders();
    }

    @Override
    public void onAttachedToWindow() {
        Log.d(TAG, "onAttachedToWindow()");
        super.onAttachedToWindow();
        attachDraweeHolders();

        transitionHandler.postDelayed(transitionRunnable, (long) (Math.random() * 5000));
    }

    @Override
    public void onFinishTemporaryDetach() {
        Log.d(TAG, "onFinishTEmporaryDetach()");
        super.onFinishTemporaryDetach();
        attachDraweeHolders();
    }

    private void detachDraweeHolders() {
        for (DraweeHolder dh : mMyMultiHolders) {
            dh.onDetach();
        }
    }

    private void attachDraweeHolders() {
        for (DraweeHolder dh : mMyMultiHolders) {
            dh.onAttach();
        }
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
        mInfoBounds = mDigestCellLayout.getInfoRect(mLayoutBounds);
        mInfoPanel.setBounds(mInfoBounds);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout");
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw()");

        super.onDraw(canvas);

        synchronized (this) {
            for (int i = 0; i < mImagePanelCount; i++) {
                Drawable drawable = mMyMultiHolders.get(i).getTopLevelDrawable();
                drawable.setBounds(mChildrenBounds.get(i));
                drawable.draw(canvas);
            }

//            Rect infoBounds = mDigestCellLayout.getInfoRect(mLayoutBounds);

            drawInfoPanel(canvas, 255, 128);
        }

    }

    private void drawInfoPanel(Canvas canvas, int alphaText, int alphaBackground) {
        mInfoPanel.draw(canvas, alphaText, alphaBackground);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        super.verifyDrawable(who);
        Log.d(TAG, "verifyDrawable()");
        for (int i = 0; i < mMyMultiHolders.size(); i++) {
            if (who == mMyMultiHolders.get(i).getTopLevelDrawable()) {
                return true;
            }
        }
        return false;
    }

    // Drawee stuff

    private GenericDraweeHierarchy createDraweeHierarchy() {
        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .setFadeDuration(10)
                .build();
        hierarchy.setPlaceholderImage(getResources().getDrawable(R.drawable.photo_offline_large), ScalingUtils.ScaleType.CENTER_INSIDE);
        return hierarchy;
    }

    private void setMultiDraweeSource(int position) {
        Log.d(TAG, "setMultiDraweeSource()");

        synchronized (this) {
            Log.d(TAG, "mPanelCount = " + mImagePanelCount);
            for (int i = 0; i < mImagePanelCount; i++) {
                // have to build a new DraweeController if you want to set new URI. See setImageURI function in SimpleDraweeView.java for instance.
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(PlaceholderContent.getUri(((i + 1) * (position + 1))))
                        .build();

                if (i >= mMyMultiHolders.size()) {          // there is no DraweeHolder available to be reuse: create a new DraweeHolder, and add into the MultiDraweeHolder
                    DraweeHolder dh = DraweeHolder.create(createDraweeHierarchy(), getContext());
                    dh.setController(controller);
                    dh.getTopLevelDrawable().setCallback(this);     // a holder is set to the view for the first time, set the callback to its top-level drawable.
                    // FIXME: 1/20/16 where shall we clear the callback (setCallback(null))? when the view is not needed anymore. Call clear in DigestRecyclerView??
                    mMyMultiHolders.add(dh);
                } else {        // there is a DraweeHolder available to be reuse: simply set a new controller to this DraweeHolder.
                    DraweeHolder dh = mMyMultiHolders.get(i);
                    dh.setController(controller);
                }
            }
        }

    }

    private Runnable transitionRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "transitionRunnable(): position:" + mPosition);
            synchronized (this) {
//                DraweeHolder dh = mMultiDraweeHolder.get(0);

                int index = 0;
                DraweeHolder dh = mMyMultiHolders.get(index);

                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(PlaceholderContent.getUri((((int) (Math.random() * 10) + 1) * (mPosition + 1))))
                        .build();
                dh.setController(controller);

                mMyMultiHolders.set(index, dh);         // there is NOT a "set" function in Fresco's MultiDraweeHolder, so we use our own DraweeHolder arraylist instead.

//                invalidate();
                transitionHandler.postDelayed(this, ROTATE_FREQUENCY);
            }
        }
    };

    class InfoPanel {

        private final TextPaint mDayPaint = new TextPaint();
        private final TextPaint mMonthPaint = new TextPaint();
        private final Paint mColorPaint = new Paint();

        Rect mBounds;
        float mDayTextSize;
        float mMonthTextSize;
        String mDay;
        String mMonth;

        public InfoPanel() {

            mDayTextSize = 120;
            mMonthTextSize = 100;

            mDayPaint.setColor(Color.WHITE);
            mDayPaint.setAntiAlias(true);

            mMonthPaint.setColor(Color.WHITE);
            mMonthPaint.setAntiAlias(true);

            mColorPaint.setStyle(Paint.Style.FILL);
        }

        public void setBounds(Rect bounds) {
            mBounds = bounds;

            if (mBounds != null) {
                mBounds.inset(mPanelPadding, mPanelPadding);
            }
        }

        public void setTimestamp(long timestamp) {
            mDay = getDayString(timestamp);
            mMonth = getMonthString(timestamp);
        }

        void draw(Canvas canvas, int alphaText, int alphaBackground) {
            if (hasInfo()) {
                mDayPaint.setAlpha(alphaText);
                mMonthPaint.setAlpha(alphaText);
                mColorPaint.setColor(ColorUtils.getCompanyColor(mPosition));
                mColorPaint.setAlpha(alphaBackground);
                canvas.drawRect(mBounds, mColorPaint);

                drawInfoStacked(canvas);
            }
        }

        void drawInfoStacked(Canvas canvas) {
            int xDay = mBounds.left + mBounds.width() / 2;
            int yDay = mBounds.top + (int) (mBounds.height() / 2 - (mDayPaint.descent() + mDayPaint.ascent() / 2));
            int xMonth = mBounds.left + mBounds.width() / 2;
            int yMonth = mBounds.top + (int) (mBounds.height() / 2 - (mMonthPaint.descent() + mMonthPaint.ascent() / 2) + mMonthTextSize);


            if (mDay != null && mMonth != null) {
                mDayPaint.setTextAlign(Paint.Align.CENTER);
                mDayPaint.setTextSize(mDayTextSize);
                canvas.drawText(mDay, xDay, yDay, mDayPaint);

                mMonthPaint.setTextAlign(Paint.Align.CENTER);
                mMonthPaint.setTextSize(mMonthTextSize);
                canvas.drawText(mMonth, xMonth, yMonth, mMonthPaint);
            }
        }

        public boolean hasInfo() {
            return mBounds != null;
        }

        String getDayString(long timestamp) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(timestamp);
            return String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        }

        String getMonthString(long timestamp) {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(timestamp);
            return calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault());
        }
    }



}
