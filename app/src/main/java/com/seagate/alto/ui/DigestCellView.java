package com.seagate.alto.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
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
import com.facebook.drawee.view.MultiDraweeHolder;
import com.seagate.alto.PlaceholderContent;
import com.seagate.alto.R;
import com.seagate.alto.utils.ColorUtils;
import com.seagate.alto.utils.LayoutUtils;
import com.seagate.alto.utils.ScreenUtils;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DigestCellView extends View {

    private final static String TAG = DigestCellView.class.getName();

    private MultiDraweeHolder<GenericDraweeHierarchy> mMultiDraweeHolder;
//    private DraweeHolder mInfoDraweeHolder;
    private final InfoPanel mInfoPanel = new InfoPanel();


    private static final int CURSOR_WINDOW_SIZE = 20;
    private static final int MAX_CYCLING_RANGE = 20;
    private int mRetryCount = 0;

    enum TransitionType {None, Crossfade, FlipHorizontal, FlipVertical}

    private static final int MAX_COLLAGES_ON_SCREEN = 5;
    private static final int ROTATE_FREQUENCY = 5000; // ms

    private static final Paint sPlaceholderPaint = new Paint();
    private static final TextPaint sTextPaint = new TextPaint();

    // Rotate stuff
    private final Paint mBackgroundPaint = new Paint();


    private long mDigestId;
    private int mImagePanelCount;
    private int mInforPanelCount = 1;
    private int mPosition;
    private Cursor mContentCursor;
    private int mContentCursorCount = 0;
    private final Object mDigestCellItemsLock = new Object();

    private int mCellBorder;
    private int mPanelPadding;

    private int mLastOrientation;

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

        mMultiDraweeHolder = new MultiDraweeHolder<>();

        mCellBorder = LayoutUtils.getBorderSize(getWidth());
        mPanelPadding = LayoutUtils.getPanelPadding(getWidth());
    }


    public void loadContent(int position) {

        mPosition = position;
        Log.d(TAG, "loadContent()");
        setMultiDraweeSource(mPosition);
        invalidate();
        Log.d(TAG, "invalidate()");
    }


    @Override
    public void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow()");
        super.onDetachedFromWindow();
        detachdraweeHolders();
    }

    @Override
    public void onStartTemporaryDetach() {
        Log.d(TAG, "onStartTemporaryDetach()");
        super.onStartTemporaryDetach();
        detachdraweeHolders();
    }

    @Override
    public void onAttachedToWindow() {
        Log.d(TAG, "onAttachedToWindow()");
        super.onAttachedToWindow();
        attachDraweeHolders();
    }

    @Override
    public void onFinishTemporaryDetach() {
        Log.d(TAG, "onFinishTEmporaryDetach()");
        super.onFinishTemporaryDetach();
        attachDraweeHolders();
    }

    private void detachdraweeHolders() {
        mMultiDraweeHolder.onDetach();
    }

    private void attachDraweeHolders() {
        mMultiDraweeHolder.onAttach();
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure()");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size;
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        int margin = lp.leftMargin;
        if (ScreenUtils.getOrientation() != mLastOrientation) {
            mLastOrientation = ScreenUtils.getOrientation();
        }
        if (ScreenUtils.isPortrait()) {
            size = ScreenUtils.getWidthInPixels();
        } else {        // FIXME: 1/11/16 landscape is not done yet.
            final TypedArray styledAttributes = getContext().getTheme().obtainStyledAttributes(new int[] { android.R.attr.actionBarSize });
            int actionBarHeight = (int) styledAttributes.getDimension(0, 0);
            styledAttributes.recycle();
            size = ScreenUtils.getHeightInPixels() - actionBarHeight;
//                    - LayoutUtils.getStatusBarHeight();
        }

        Log.i("DigestCellView", "onMeasure size: " + size);
        setMeasuredDimension(size - 2 * margin, size - 2 * margin);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.d(TAG, "onLayout");
        super.onLayout(changed, left, top, right, bottom);

    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw()");
        // View might be in the process of being recycled. If so, it will be detached from it's parent.
        super.onDraw(canvas);
        if (getParent() == null) {
            invalidate();
            return;
        }

        if (!ScreenUtils.isPortrait()) {
            canvas.rotate(90, getWidth() / 2, getHeight() / 2);
        }

        synchronized (this) {
            DigestCellLayout digestCellLayout = DigestCellLayouts.getLayoutForSize(mImagePanelCount, mPosition);
            Rect layoutBounds = new Rect(0, 0, getWidth(), getHeight());
            layoutBounds.inset(mCellBorder, mCellBorder);
            for (int i = 0; i < mImagePanelCount; i++) {
                Drawable drawable = mMultiDraweeHolder.get(i).getTopLevelDrawable();
                Rect childBounds = digestCellLayout.getPanelRect(i, layoutBounds);
                childBounds.inset(mPanelPadding, mPanelPadding);
                drawable.setBounds(childBounds);
                drawable.draw(canvas);
            }
//            mMultiDraweeHolder.draw(canvas);              // don't do MultiDraweeHolder.draw(canvas) here, because we may have more DraweeHolders than we want to display.

            Rect infoBounds = digestCellLayout.getInfoRect(layoutBounds);

            long offset = Timestamp.valueOf("2012-01-01 00:00:00").getTime();
            long end = Timestamp.valueOf("2013-01-01 00:00:00").getTime();
            long diff = end - offset + 1;
            long rand = offset + (long)(Math.random() * diff);      // FIXME: 1/14/16 getTimeStamp here

            mInfoPanel.reset(rand, infoBounds);
            drawInfo(canvas, 255);
        }

    }


    // draw
    private void drawInfo(Canvas canvas, int alpha) {
        mInfoPanel.draw(canvas, alpha);
    }


    // Drawee stuff

    private GenericDraweeHierarchy createDraweeHierarchy() {
        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .setFadeDuration(10)
                .build();
        hierarchy.setPlaceholderImage(R.drawable.photo_offline_large);
//        RoundingParams roundingParams = new RoundingParams();
//        roundingParams.setBorder(R.color.red, 2.0f);
//        hierarchy.setRoundingParams(roundingParams);
        return hierarchy;
    }

    private void setMultiDraweeSource(int position) {
        Log.d(TAG, "setMultiDraweeSource()");

        synchronized (this) {
            mImagePanelCount = position % MAX_COLLAGES_ON_SCREEN + 1;       // TODO: 1/14/16 get image panel count here.
            Log.d(TAG, "mPanelCount = " + mImagePanelCount);
            for (int i = 0; i < mImagePanelCount; i++) {

                // TODO: 1/14/16 can we only set new uri to the same DraweeHolder instead of set new DraweeHolder??
                DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setUri(PlaceholderContent.getUri(((i + 1) * (position + 1))))
                        .build();
                if (i < mMultiDraweeHolder.size()) {        // there is a DraweeHolder available to be reuse: simply set a new controller to this DraweeHolder.
                    DraweeHolder dh = mMultiDraweeHolder.get(i);
                    dh.setController(controller);
                } else {                                    // there is no DraweeHolder available to be reuse: create a new DraweeHolder, and add into the MultiDraweeHolder
                    DraweeHolder dh = DraweeHolder.create(createDraweeHierarchy(), getContext());
                    dh.setController(controller);
                    mMultiDraweeHolder.add(dh);
                }

//                DraweeController controller = Fresco.newDraweeControllerBuilder()
//                        .setUri(PlaceholderContent.getUri(((i + 1) * (position + 1))))
//                        .build();
//                DraweeHolder dh = DraweeHolder.create(createDraweeHierarchy(), getContext());
//                dh.setController(controller);
//                mMultiDraweeHolder.add(dh);
            }

            mMultiDraweeHolder.onAttach();
            Log.d(TAG, "After setMultiDraweeSource -- mMultiDraweeHolder: " + mMultiDraweeHolder + ", size()" + mMultiDraweeHolder.size());

        }


    }

    // private

    private int getCursorCount() {
        return mContentCursorCount;     // = mContentCursor.getCount();
    }


    class CollageItem {
//        ContentItem mItem;
        Rect mItemBounds;
        int mViewPosition;
        Cursor mCursor;
        int mCursorPosition;
        int mCollagePosition;
        int[] mRotation = new int[2];
        Bitmap[] mTransitionBitmaps = new Bitmap[2];
        Paint[] mTransitionPaints = new Paint[2];
        TransitionType mTransitionType;
        boolean mTransitionFromCurrent;
        boolean mTransitionInProgress;
        long mTransitionStart;
        boolean mChain;
        boolean mUseLocal;
//        final LoadInfo mLoadInfo;
//        final ThumbnailLoadListener mLoadListener;

        public CollageItem() {
            mTransitionPaints[0] = new Paint();
            mTransitionPaints[0].setAntiAlias(true);
            mTransitionPaints[0].setFilterBitmap(true);
            mTransitionPaints[0].setDither(true);
            mTransitionPaints[0].setStyle(Paint.Style.FILL);
            mTransitionPaints[0].setColor(Color.BLACK);

            mTransitionPaints[1] = new Paint();
            mTransitionPaints[1].setAntiAlias(true);
            mTransitionPaints[1].setFilterBitmap(true);
            mTransitionPaints[1].setDither(true);

//            mLoadInfo = new LoadInfo();
//            mLoadListener = new ThumbnailLoadListener(this);
        }

        public void reset(boolean transitionFromCurrent, boolean chain, boolean useLocal) {
//            mLoadInfo.reset();
            mTransitionFromCurrent = transitionFromCurrent;
            mChain = chain;
            mUseLocal = useLocal;
        }
    }

    class InfoPanel {
        final int ORIENTATION_SQUARE = -1;
        final int ORIENTATION_PORTRAIT = 0;
        final int ORIENTATION_LANDSCAPE = 1;

        final int JUSTIFICATION_TOPLEFT = 0;
        final int JUSTIFICATION_CENTER = 1;
        final int JUSTIFICATION_BOTOMRIGHT = 2;

        private final TextPaint mDayPaint = new TextPaint();
        private final TextPaint mMonthPaint = new TextPaint();
        private final Paint mColorPaint = new Paint();

        Rect mBounds;
//        final Rect mTextBounds = new Rect();
        float mDayTextSize;
        float mMonthTextSize;
        String mDay;
        String mMonth;
        int mOrientation;

        public InfoPanel() {

            mDayPaint.setColor(Color.WHITE);
            mDayPaint.setAntiAlias(true);
//            mDayPaint.setTypeface(FontUtils.getFont(family));

            mMonthPaint.setColor(Color.WHITE);
            mMonthPaint.setAntiAlias(true);
//            mMonthPaint.setTypeface(FontUtils.getFont(family));

            mColorPaint.setStyle(Paint.Style.FILL);
        }

        public void reset(long timestamp, Rect bounds) {
            mBounds = bounds;

            if (mBounds != null) {
                mDay = getDayString(timestamp);
                mMonth = getMonthString(timestamp);

                mBounds.inset(mPanelPadding, mPanelPadding);

                mOrientation = getOrientation(mBounds);

//                justifyTextBounds(mOrientation, mPosition);
            }
        }

        void draw(Canvas canvas, int alpha) {
            if (hasInfo()) {
                mDayPaint.setAlpha(alpha);
                mMonthPaint.setAlpha(alpha);
                mColorPaint.setColor(ColorUtils.getCompanyColor(mPosition));
                mColorPaint.setAlpha(128);
                canvas.drawRect(mBounds, mColorPaint);

                setTextSizeStacked();
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

        void setTextSizeStacked() {
            mDayTextSize = 120;
            mMonthTextSize = 100;
        }

        int getOrientation(Rect bounds) {
            int difference = Math.abs(bounds.width() - bounds.height());
            // Allow for some rounding errors...
            if (difference > 10) {
                if (bounds.width() >= 2 * bounds.height()) {
                    return ORIENTATION_LANDSCAPE;
                } else if (bounds.height() >= 2 * bounds.width()) {
                    return ORIENTATION_PORTRAIT;
                }
            }

            return ORIENTATION_SQUARE;
        }

        public boolean hasInfo() {
            return mBounds != null;
        }

        int getSmallestDimension() {
            return Math.min(mBounds.width(), mBounds.height());
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
