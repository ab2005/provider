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
import android.net.Uri;
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
import com.seagate.alto.utils.LayoutUtils;
import com.seagate.alto.utils.ScreenUtils;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class DigestCellView extends View {

    private DraweeHolder mFirstDraweeHolder;
    private DraweeHolder mSecondDraweeHolder;
    private DraweeHolder mThirdDraweeHolder;
    private MultiDraweeHolder<GenericDraweeHierarchy> mMultiDraweeHolder;

    private final static String TAG = DigestCellView.class.getName();
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

    private final InfoPanel mInfoPanel = new InfoPanel();

    private long mDigestId;
    private int mPosition = 1;
    private Cursor mContentCursor;
    private int mContentCursorCount = 0;
    private final Random mRandom = new Random();

    final Rect mViewBounds = new Rect();
    final Rect mShadowBounds = new Rect();
    final Rect mContentBounds = new Rect();
    private Rect mContentBackground;
    private boolean mLayoutComplete;
    private boolean mFullyLoaded;
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
//        int padding = 5;
//        setPadding(padding, padding, padding, padding);

        mFirstDraweeHolder = DraweeHolder.create(createDraweeHierarchy(), context);
        mSecondDraweeHolder = DraweeHolder.create(createDraweeHierarchy(), context);
        mThirdDraweeHolder = DraweeHolder.create(createDraweeHierarchy(), context);
        mMultiDraweeHolder = new MultiDraweeHolder<>();
    }


    public void loadContent(int position) {


        setMultiDraweeSource(position);
        invalidate();
    }


    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        detachdraweeHolders();
    }

    @Override
    public void onStartTemporaryDetach() {
        super.onStartTemporaryDetach();
        detachdraweeHolders();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        attachDraweeHolders();
    }

    @Override
    public void onFinishTemporaryDetach() {
        super.onFinishTemporaryDetach();
        attachDraweeHolders();
    }

    private void detachdraweeHolders() {
        mFirstDraweeHolder.onDetach();
        mSecondDraweeHolder.onDetach();
        mThirdDraweeHolder.onDetach();
        mMultiDraweeHolder.onDetach();
    }

    private void attachDraweeHolders() {
        mFirstDraweeHolder.onAttach();
        mSecondDraweeHolder.onAttach();
        mThirdDraweeHolder.onAttach();
        mMultiDraweeHolder.onAttach();
    }


    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size;
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        int margin = lp.leftMargin;
        if (ScreenUtils.getOrientation() != mLastOrientation) {
            mLastOrientation = ScreenUtils.getOrientation();
            mLayoutComplete = false;
        }
        if (ScreenUtils.isPortrait()) {
            size = ScreenUtils.getWidthInPixels();
        } else {
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
        super.onLayout(changed, left, top, right, bottom);
//        mViewBounds.set(0, 0, getWidth(), getHeight());
//
//        mContentBackground = new Rect(mViewBounds);
//        int shadowSize = Math.round(mContentBackground.height() * 0.02f);
//        mContentBackground.inset(shadowSize, shadowSize);
//        mShadowBounds.set(mContentBackground);
//        mShadowBounds.bottom += shadowSize;
//
//        mContentBounds.set(mContentBackground);
//        int borderSize = LayoutUtils.getBorderSize(mContentBounds.width());
//        mContentBounds.inset(borderSize, borderSize);
//
//        if (!mLayoutComplete) {
//            mLayoutComplete = true;
////            refresh();
//        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        // View might be in the process of being recycled. If so, it will be detached from it's parent.
        super.onDraw(canvas);
        if (getParent() == null) {
            invalidate();
            return;
        }

        if (!ScreenUtils.isPortrait()) {
            canvas.rotate(90, getWidth() / 2, getHeight() / 2);
        }

//        setMultiDraweeSource(1);
        // TODO: 1/7/16 Draw placeholder here, and replace with real photos in loadContent() call (not sure about this.)

        Drawable drawable;

        drawable = mFirstDraweeHolder.getTopLevelDrawable();
        drawable.setBounds(0, 0, getWidth() / 2, getHeight() / 2);
        drawable.draw(canvas);

        drawable = mSecondDraweeHolder.getTopLevelDrawable();
        drawable.setBounds(getWidth() / 2, 0, getWidth(), getHeight() / 2);
        drawable.draw(canvas);

        drawable = mThirdDraweeHolder.getTopLevelDrawable();
        drawable.setBounds(0, getHeight() / 2, getWidth(), getHeight());
        drawable.draw(canvas);

    }



    // Drawee stuff

    private GenericDraweeHierarchy createDraweeHierarchy() {
        return new GenericDraweeHierarchyBuilder(getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .setFadeDuration(0)
                .build();
    }

    private void setFirstDraweeSource(Uri uri) {
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setOldController(mFirstDraweeHolder.getController())
                .build();
        mFirstDraweeHolder.setController(controller);
        mMultiDraweeHolder.add(mFirstDraweeHolder);
    }

    private void setSecondDraweeSource(Uri uri) {
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setOldController(mSecondDraweeHolder.getController())
                .build();
        mSecondDraweeHolder.setController(controller);
        mMultiDraweeHolder.add(mSecondDraweeHolder);
    }

    private void setThirdDraweeSource(Uri uri) {
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setOldController(mThirdDraweeHolder.getController())
                .build();
        mThirdDraweeHolder.setController(controller);
        mMultiDraweeHolder.add(mThirdDraweeHolder);
    }

    private void setMultiDraweeSource(int position) {
        setFirstDraweeSource(PlaceholderContent.getUri(1 * (position+1)));
        setSecondDraweeSource(PlaceholderContent.getUri(2 * (position+1)));
        setThirdDraweeSource(PlaceholderContent.getUri(3 * (position+1)));
        mFirstDraweeHolder.onAttach();
        mSecondDraweeHolder.onAttach();
        mThirdDraweeHolder.onAttach();
        mMultiDraweeHolder.onAttach();
    }





    // private

    private int getCursorCount() {
        return mContentCursorCount;     // = mContentCursor.getCount();
    }












    void drawBackground(Canvas canvas, int alpha) {
        mBackgroundPaint.setAlpha(alpha);
//        mBackgroundPaint.setColor(ColorUtils.getCompanyColor(mPosition));
        canvas.drawRect(mContentBackground, mBackgroundPaint);
    }

    void drawInfo(Canvas canvas, int alpha) {
        mInfoPanel.draw(canvas, alpha);
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

        Rect mBounds;
        final Rect mTextBounds = new Rect();
        float mDayTextSize;
        float mMonthTextSize;
        String mDay;
        String mMonth;
        int mOrientation;
        int mPadding;

        public InfoPanel() {

            mDayPaint.setColor(Color.WHITE);
            mDayPaint.setAntiAlias(true);
//            mDayPaint.setTypeface(FontUtils.getFont(family));

            mMonthPaint.setColor(Color.WHITE);
            mMonthPaint.setAntiAlias(true);
//            mMonthPaint.setTypeface(FontUtils.getFont(family));
        }

        public void reset(long timestamp, Rect bounds) {
            mBounds = bounds;

            if (mBounds != null) {
                mDay = getDayString(timestamp);
                mMonth = getMonthString(timestamp);

                mPadding = LayoutUtils.getPanelPadding(mBounds.width());
                mBounds.inset(mPadding, mPadding);

                mOrientation = getOrientation(mBounds);

                justifyTextBounds(mOrientation, mPosition);
            }
        }

        void draw(Canvas canvas, int alpha) {
            if (hasInfo()) {
                mDayPaint.setAlpha(alpha);
                mMonthPaint.setAlpha(alpha);

                if (mOrientation == ORIENTATION_LANDSCAPE) {
                    setTextSizeLinear();
                    drawInfoLinear(canvas);
                } else {
                    setTextSizeStacked();
                    drawInfoStacked(canvas);
                }
            }
        }

        void drawInfoLinear(Canvas canvas) {
            int x = mTextBounds.left + mTextBounds.width() / 2;
            int y = mTextBounds.bottom - mTextBounds.height() / 4;

            mDayPaint.setTextAlign(Paint.Align.RIGHT);
            mDayPaint.setTextSize(mDayTextSize);
            canvas.drawText(mDay, x - mDayTextSize * 0.1f, y, mDayPaint);

            mMonthPaint.setTextAlign(Paint.Align.LEFT);
            mMonthPaint.setTextSize(mMonthTextSize);
            canvas.drawText(mMonth, x + mMonthTextSize * 0.1f, y, mMonthPaint);
        }

        void setTextSizeLinear() {
            int smallestDim = getSmallestDimension();
            if (smallestDim == mBounds.width()) {
                mDayTextSize = (float)smallestDim * 0.2f;
            } else {
                mDayTextSize = (float)smallestDim * 0.7f;
            }

            mMonthTextSize = mDayTextSize * 0.7f;
        }

        void drawInfoStacked(Canvas canvas) {
            int x = mTextBounds.left + mTextBounds.width() / 2;
            int y = mTextBounds.bottom - mTextBounds.height() / 2;

            mDayPaint.setTextAlign(Paint.Align.CENTER);
            mDayPaint.setTextSize(mDayTextSize);
            if (mDay != null && mMonth != null) {
                canvas.drawText(mDay, x, y, mDayPaint);

                mMonthPaint.setTextAlign(Paint.Align.CENTER);
                mMonthPaint.setTextSize(mMonthTextSize);
                canvas.drawText(mMonth, x, y + mMonthTextSize, mMonthPaint);
            }
        }

        void setTextSizeStacked() {
            mDayTextSize = mTextBounds.height() / 2 * 0.9f;
            mMonthTextSize = mDayTextSize * 0.7f;
        }

        void justifyTextBounds(int orientation, int index) {
            int justification = index % 3;
            if (orientation == ORIENTATION_SQUARE) {
                justification = JUSTIFICATION_CENTER;
            }

            initTextBounds(orientation);

            if (orientation == ORIENTATION_SQUARE) {
                return;
            }

            switch (justification) {
                case JUSTIFICATION_TOPLEFT:
                    break;

                case JUSTIFICATION_CENTER:
                    if (orientation == ORIENTATION_LANDSCAPE) {
                        mTextBounds.offset(mTextBounds.width(), 0);
                    } else {
                        mTextBounds.offset(0, mTextBounds.height());
                    }
                    break;

                case JUSTIFICATION_BOTOMRIGHT:
                    if (orientation == ORIENTATION_LANDSCAPE) {
                        mTextBounds.offset(mTextBounds.width() * 2, 0);
                    } else {
                        mTextBounds.offset(0, mTextBounds.height() * 2);
                    }
                    break;
            }

        }

        void initTextBounds(int orientation) {
            mTextBounds.left = mBounds.left;
            mTextBounds.top = mBounds.top;
            if (orientation == ORIENTATION_LANDSCAPE) {
                mTextBounds.right = mTextBounds.left + mBounds.width() / 3;
                mTextBounds.bottom = mTextBounds.top + mBounds.height();
            } else if (orientation == ORIENTATION_PORTRAIT) {
                mTextBounds.right = mTextBounds.left + mBounds.width();
                mTextBounds.bottom = mTextBounds.top + mBounds.height() / 3;
            } else {
                mTextBounds.right = mTextBounds.left + mBounds.width();
                mTextBounds.bottom = mTextBounds.top + mBounds.height();
            }

            mTextBounds.inset(mPadding, mPadding);
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
