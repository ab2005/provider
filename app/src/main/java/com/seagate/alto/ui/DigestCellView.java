package com.seagate.alto.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.seagate.alto.utils.LayoutUtils;
import com.seagate.alto.utils.ScreenUtils;

import java.util.Calendar;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.Timer;

public class DigestCellView extends View {


    private final static String TAG = DigestCellView.class.getName();
    private static final int CURSOR_WINDOW_SIZE = 20;
    private static final int MAX_CYCLING_RANGE = 20;
    private int mRetryCount = 0;

    enum TransitionType {None, Crossfade, FlipHorizontal, FlipVertical}
    public final int FROM = 0;
    public final int TO = 1;
    private static final int CROSSFADE_DURATION = 2000;
    private static final int FADEIN_DURATION = 300;
    private static final int FLIP_DURATION = 250;

    private static final int MAX_COLLAGES_ON_SCREEN = 5;
    private static final int ROTATE_FREQUENCY = 5000; // ms

    private static Bitmap sVideoIndicator;
    private static NinePatchDrawable sShadow;
    private static NinePatchDrawable sBevel;
    private static final Paint sPlaceholderPaint = new Paint();
    private static final TextPaint sTextPaint = new TextPaint();

    // Rotate stuff
    private final Random mRandom = new Random();
    private Timer mTimer;
    private int mLastPanel = -1;
    private int mPanelCount;
    private final Paint mBackgroundPaint = new Paint();

    private final InfoPanel mInfoPanel = new InfoPanel();

    private long mDigestId;
    private int mPosition = 1;
    private Cursor mContentCursor;
    private int mContentCursorCount = 0;
//    private Query mQuery;
    private long mAlbumTimestamp;
    private final CollageItem[] mCollageItems = new CollageItem[MAX_COLLAGES_ON_SCREEN];
    private final Object mCollageItemsLock = new Object();
    private final Object mTimerLock = new Object();
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
        this(context, attrs, 0);
    }

    public DigestCellView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        setWillNotDraw(false);
//        setDrawingCacheEnabled(false);
//
//        sVideoIndicator = ((BitmapDrawable)getResources().getDrawable(R.drawable.video_play_indicator_mini)).getBitmap();
//
//        sShadow = (NinePatchDrawable)getResources().getDrawable(R.drawable.thumbnail_shadow);
//        sBevel = (NinePatchDrawable)getResources().getDrawable(R.drawable.thumbnail_bevel);
//
//        sTextPaint.setTextAlign(Paint.Align.CENTER);
//        sTextPaint.setColor(Color.RED);
//
//        sPlaceholderPaint.setAntiAlias(true);
//        sPlaceholderPaint.setStyle(Paint.Style.FILL);
//        sPlaceholderPaint.setColor(Color.BLACK);
//        sPlaceholderPaint.setAlpha(64); // 25%
//
//        mBackgroundPaint.setAntiAlias(true);
//        mBackgroundPaint.setStyle(Paint.Style.FILL);
//
//        for (int i = 0; i < mCollageItems.length; i++) {
//            mCollageItems[i] = new CollageItem();
//        }
    }


    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
//        ThreadPoolUtils.execute(new Runnable() {
//            @Override
//            public void run() {
//                synchronized (getCursorLock()) {
//                    closeCursor();
//                }
//            }
//        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size;
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
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        mViewBounds.set(0, 0, getWidth(), getHeight());

        mContentBackground = new Rect(mViewBounds);
        int shadowSize = Math.round(mContentBackground.height() * 0.02f);
        mContentBackground.inset(shadowSize, shadowSize);
        mShadowBounds.set(mContentBackground);
        mShadowBounds.bottom += shadowSize;

        mContentBounds.set(mContentBackground);
        int borderSize = LayoutUtils.getBorderSize(mContentBounds.width());
        mContentBounds.inset(borderSize, borderSize);

        if (!mLayoutComplete) {
            mLayoutComplete = true;
//            refresh();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // View might be in the process of being recycled. If so, it will be detached from it's parent.
        if (getParent() == null) {
            invalidate();
            return;
        }

        if (!ScreenUtils.isPortrait()) {
            canvas.rotate(90, getWidth() / 2, getHeight() / 2);
        }

//        synchronized (mCollageItemsLock) {
//            // Assume we're not loaded until we are...
//            mFullyLoaded = false;
//            boolean fullyLoaded = true;
//            for (int i = 0; i < mPanelCount; i++) {
//                CollageItem item = mCollageItems[i];
//                drawPlaceholder(item, canvas);
//
//                if (item != null && item.mItem != null) {
//                    if (item.mTransitionInProgress) {
//                        handleTransition(item, canvas);
//                    } else {
//                        drawCollageFrame(item, canvas, 255);
//                        drawItemScaled(item, FROM, canvas);
//                    }
//                } else {
//                    // Somebody is still null...
//                    fullyLoaded = false;
//                    if (i == 0) {
//                        drawCollageFrame(i, canvas, 255);
//                    }
//                }
//            }
//            mFullyLoaded = fullyLoaded;
//        }
    }

    void drawPlaceholder(CollageItem item, Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT);
        if (item.mItemBounds != null) {
            canvas.drawRect(item.mItemBounds, sPlaceholderPaint);
        }
    }
//
//    void drawItemScaled(CollageItem item, int index, Canvas canvas) {
//        Bitmap bitmap = item.mTransitionBitmaps[index];
//        Paint paint = item.mTransitionPaints[index];
//
//        int originalWidth = bitmap == null ? 0 : bitmap.getWidth();
//        int originalHeight = bitmap == null ? 0 : bitmap.getHeight();
//        int reqWidth = item.mItemBounds.width();
//        int reqHeight = item.mItemBounds.height();
//
//        // Scale the bitmap proportionately to fill the bounds
//        float scale = BitmapUtils.calculateScaleFactor(originalWidth, originalHeight, reqWidth, reqHeight);
//        float scaledWidth = originalWidth * scale;
//        float scaledHeight = originalHeight * scale;
//
//        // Center the scaled image in the bitmap horizontally
//        float transX = scaledWidth > reqWidth ? ((scaledWidth - reqWidth) / 2) : 0;
//        // Don't center vertically, favor top of image to avoid chopping off heads AVERY-81
//        float transY = scaledHeight > reqHeight ? ((scaledHeight - reqHeight) / 6) : 0;
//
//        // Use the matrix to do all the work
//        Matrix matrix = new Matrix();
//        matrix.preTranslate(item.mItemBounds.left - transX, item.mItemBounds.top - transY);
//        matrix.preScale(scale, scale);
//
//        Status status = item.mItem.getThumbnailUriStatus();
//        StatusUtils.Type type = StatusUtils.getType(status);
//        boolean showStatus = type != null && type == StatusUtils.Type.Error || type == StatusUtils.Type.Offline;
//
//        // Draw and we're done
//        if (!showStatus) {
//            canvas.save();
//            canvas.clipRect(item.mItemBounds);
//            if (bitmap != null && !bitmap.isRecycled()) {
//                canvas.drawBitmap(bitmap, matrix, paint);
//                if (item.mItem.isVideo() && (!item.mTransitionInProgress || index == TO)) {
//                    drawVideoIndicator(item, canvas, paint);
//                }
//            }
//            canvas.restore();
//        } else {
//            drawStatusThumbnail(item, canvas, index, paint);
//        }
//
//        drawDebugInfo(item, canvas, index);
//    }

    void drawCollageFrame(CollageItem item, Canvas canvas, int alpha) {
        drawCollageFrame(item.mCollagePosition, canvas, alpha);
    }

    void drawCollageFrame(int index, Canvas canvas, int alpha) {
        // Only draw the shadow and background right before we draw the first thumbnail of the
        // batch. Be sure it's only the "from" thumbnail otherwise the shadow and background
        // will draw over the "from" thumbnail during the "to" transition.
        if (index == 0) {
            drawShadow(canvas);
            drawBackground(canvas, alpha);
            drawInfo(canvas, alpha);
            drawPlaceholder(mCollageItems[0], canvas);
        }
    }

    void drawShadow(Canvas canvas) {
        sShadow.setAlpha(200);
        sShadow.setBounds(mShadowBounds);
        sShadow.draw(canvas);
    }

    void drawBevel(Canvas canvas, int alpha) {
        sBevel.setAlpha(Math.min(128, alpha));
        sBevel.setBounds(mContentBackground);
        sBevel.draw(canvas);
    }

    void drawBackground(Canvas canvas, int alpha) {
        mBackgroundPaint.setAlpha(alpha);
//        mBackgroundPaint.setColor(ColorUtils.getCompanyColor(mPosition));
        canvas.drawRect(mContentBackground, mBackgroundPaint);
    }

    void drawInfo(Canvas canvas, int alpha) {
        mInfoPanel.draw(canvas, alpha);
    }

//    void drawStatusThumbnail(CollageItem item, Canvas canvas, int index, Paint paint) {
//        synchronized (mCollageItemsLock) {
//            Status status = item.mItem.getThumbnailUriStatus();
//            StatusUtils.Type type = StatusUtils.getType(status);
//            Rect destination = new Rect(item.mItemBounds);
//            destination.inset(destination.width() / 4, destination.height() / 4);
//            Bitmap statusIcon = StatusUtils.getStatusBitmap(type, destination, item.mItem.isPhoto());
//
//            if ((!item.mTransitionInProgress || index == TO) && statusIcon != null) {
//                Rect source = new Rect(0, 0, statusIcon.getWidth(), statusIcon.getHeight());
//                destination = ViewUtils.centerRectWithin(source, destination);
//                canvas.drawBitmap(statusIcon, source, destination, paint);
//            }
//        }
//    }
//
//    void drawDebugInfo(CollageItem item, Canvas canvas, int index) {
//        synchronized (mCollageItemsLock) {
//            Status status = item.mItem.getThumbnailUriStatus();
//            StatusUtils.Type type = StatusUtils.getType(status);
//            boolean showStatus = type != null && type == StatusUtils.Type.Error || type == StatusUtils.Type.Offline;
//
//            //noinspection PointlessBooleanExpression,ConstantConditions
//            if ((!item.mTransitionInProgress || index == TO) && Settings.getInstance().getDebugThumbnails()) {
//                ViewUtils.drawTextCentered(sTextPaint, canvas, item.mItemBounds, item.mItem.getName(), ViewUtils.TextVerticalPosition.Top);
//
//                if (showStatus) {
//                    ViewUtils.drawTextCentered(sTextPaint, canvas, item.mItemBounds, status.name(), ViewUtils.TextVerticalPosition.Center);
//                }
//
//                Date dateTaken = item.mItem.getDateTaken();
//                String dateString = dateTaken == null ? getResources().getString(R.string.not_available) : DateUtils.getLongishDate(dateTaken.getTime());
//                ViewUtils.drawTextCentered(sTextPaint, canvas, item.mItemBounds, dateString, ViewUtils.TextVerticalPosition.Bottom);
//            }
//        }
//    }
//
//    private void drawVideoIndicator(CollageItem item, Canvas canvas, Paint paint) {
//        int x = item.mItemBounds.right - (sVideoIndicator.getWidth() * 4 / 3);
//        int y = item.mItemBounds.top + (sVideoIndicator.getHeight() / 4);
//        canvas.drawBitmap(sVideoIndicator, x,  y, paint);
//    }
//
//    public void transitionTo(final CollageItem item, final TransitionType transitionType) {
//        synchronized (mCollageItemsLock) {
//            item.mRotation[TO] = item.mItem.getRotation();
//            item.mTransitionBitmaps[TO] = item.mItem.getThumbnail();
//            if (item.mTransitionBitmaps[TO] == null) {
//                Log.e("ZZLOAD", "Transitioning to null thumbnail!");
//            }
//            item.mTransitionType = transitionType;
//            item.mTransitionStart = System.currentTimeMillis();
//            item.mTransitionInProgress = true;
//        }
//    }
//
//    void handleTransition(CollageItem item, Canvas canvas) {
//        long duration = System.currentTimeMillis() - item.mTransitionStart;
//        switch (item.mTransitionType) {
//            case None:
//            case Crossfade:
//                int fadeInDuration = item.mTransitionType == TransitionType.None ? 0 : item.mTransitionFromCurrent ? CROSSFADE_DURATION : FADEIN_DURATION;
//                float alpha = (float)duration / fadeInDuration;
//                int fromAlpha = 255;
//                int toAlpha = (int)(255f * alpha);
//                synchronized (mCollageItemsLock) {
//                    if (duration <= fadeInDuration) {
//                        // Transition in progress
//                        item.mTransitionPaints[FROM].setAlpha(fromAlpha);
//                        item.mTransitionPaints[TO].setAlpha(toAlpha);
//                        if (item.mTransitionFromCurrent) {
//                            drawCollageFrame(item, canvas, 255);
//                        } else {
//                            drawCollageFrame(item, canvas, toAlpha);
//                        }
//
//                        if (item.mTransitionFromCurrent && item.mTransitionBitmaps[FROM] != null) {
//                            drawItemScaled(item, FROM, canvas);
//                        } else {
//                            item.mTransitionPaints[FROM].setAlpha(255);
//                        }
//                        drawItemScaled(item, TO, canvas);
//                        drawBevel(canvas, 255);
//                        invalidate();
//                    } else {
//                        // Transition complete
//                        item.mRotation[FROM] = item.mRotation[TO];
//                        item.mTransitionBitmaps[FROM] = item.mTransitionBitmaps[TO];
//                        item.mTransitionBitmaps[TO] = null;
//                        item.mTransitionType = null;
//                        item.mTransitionInProgress = false;
//                        item.mTransitionPaints[FROM].setAlpha(255);
//                        item.mTransitionPaints[TO].setAlpha(0);
//                        drawCollageFrame(item, canvas, 255);
//                        //drawPlaceholder(item, canvas);
//                        drawItemScaled(item, FROM, canvas);
//                        drawBevel(canvas, 255);
//                    }
//                }
//                break;
//
//            case FlipHorizontal:
//                if (duration <= FLIP_DURATION) {
//                    long halfDuration = FLIP_DURATION / 2;
//                    if (duration < halfDuration) {
//                        float rotation = (float) duration / halfDuration;
//                        Matrix matrix = new Matrix();
//                        matrix.setScale(1 - rotation, 1);
//                        matrix.postTranslate((getWidth() / 2 * rotation), 0);
//                        canvas.drawBitmap(item.mTransitionBitmaps[FROM], matrix, item.mTransitionPaints[FROM]);
//                    } else {
//                        float rotation = (float) (duration - halfDuration) / halfDuration;
//                        Matrix matrix = new Matrix();
//                        matrix.setScale(rotation, 1);
//                        matrix.postTranslate(item.mTransitionBitmaps[TO].getWidth() / 2 - (getWidth() / 2 * rotation), 0);
//                        canvas.drawBitmap(item.mTransitionBitmaps[TO], matrix, item.mTransitionPaints[TO]);
//                    }
//                    invalidate();
//                } else {
//                    item.mTransitionBitmaps[FROM] = item.mTransitionBitmaps[TO];
//                    item.mTransitionBitmaps[TO] = null;
//                    item.mTransitionType = null;
//                    item.mTransitionInProgress = false;
//                    canvas.drawBitmap(item.mTransitionBitmaps[FROM], 0, 0, item.mTransitionPaints[FROM]);
//                }
//                break;
//        }
//
//    }

//    public void onPause() {
//        stopPhotoCycling();
//    }
//
//    public void onResume() {
//        startPhotoCycling();
//    }
//
//    public void loadContent(final int position, final long digestId) {
//        Log.d("ZZLOAD", "loadContent: view: " + position);
//        if (viewNeedsRefresh(digestId)) {
//            mDigestId = digestId;
//            setTag(R.id.life_collage_timestamp, mDigestId);
//            setTag(R.id.life_collage_count, 1);
//            mPosition = position;
//            mLayoutComplete = false;
//            requestLayout();
//        }
//    }
//
//    private boolean viewNeedsRefresh(long timestamp) {
//        Long currentId = (Long) getTag(R.id.life_collage_timestamp);
//        Log.i("ZZLOAD", "viewNeedsRefresh?: " + mFullyLoaded + " curId: " + currentId + " newId: " + timestamp);
//        return (!mFullyLoaded || currentId == null || currentId != timestamp);
//    }
//
//    boolean refreshCursor() {
//        synchronized (getCursorLock()) {
//            if (mDigestId != mAlbumTimestamp || mContentCursor == null || mContentCursor.isClosed()) {
//                mAlbumTimestamp = mDigestId;
//                mQuery = new Query(Query.QueryType.ByDay, mAlbumTimestamp);
//                Cursor cursor = CatalogUtils.getCursorForQuery(mQuery);
//                ((ALCursor) cursor).setWindowSize(CURSOR_WINDOW_SIZE);
//                long time = System.currentTimeMillis();
//                ((ALCursor) cursor).reset();
//                Log.i("CollageView", "refreshCursor reset took: " + (System.currentTimeMillis() - time));
//                closeCursor();
//                mRetryCount = 0;
//                mContentCursor = cursor;
//            } else {
//                long time = System.currentTimeMillis();
//                ((ALCursor) mContentCursor).reset();
//                Log.i("CollageView", "refreshCursor reset took: " + (System.currentTimeMillis() - time));
//            }
//            if (mContentCursor.isClosed()) {
//                refresh();
//                return false;
//            } else {
//                Log.d("ZZLOAD", "double checked cursor for 'closed' state");
//                mContentCursorCount = mContentCursor.getCount();
//                if (mContentCursorCount <= 0 && mRetryCount++ < 3) {
//                    refresh();
//                    return false;
//                }
//            }
//            return true;
//        }
//    }
//
//    public void refresh() {
//        ThreadPoolUtils.execute(new Runnable() {
//            @Override
//            public void run() {
//                if (refreshCursor()) {
//                    loadItems();
//                }
//            }
//        });
//    }
//
//    void loadItems() {
//        synchronized (getCursorLock()) {
//            Log.d("ZZLOAD", "loadItems: view: " + mPosition);
//            int cursorCount = getCursorCount();
//            if (cursorCount > 0) {
//                stopPhotoCycling();
//
//                Rect layoutBounds = new Rect(mContentBounds);
//                CollageLayout collageLayout = CollageLayouts.getLayoutForSize(cursorCount, mPosition);
//                mPanelCount = collageLayout.getPanelCount();
//                synchronized (mInfoPanel) {
//                    mInfoPanel.reset(mAlbumTimestamp, collageLayout.getInfoRect(layoutBounds));
//                }
//                List<Integer> photoIndices = distributeValues(0, Math.min(MAX_CYCLING_RANGE, cursorCount), mPanelCount);
//
//                if (mPanelCount == 0) {
//                    Log.e("ZZLOAD", "loadItems panel count is 0!");
//                }
//
//                for (int i = 0; i < mPanelCount; i++) {
//                    Rect childBounds = getChildBounds(i, collageLayout, layoutBounds);
//
//                    synchronized (mCollageItemsLock) {
//                        mCollageItems[i].mItem = null;
//                        mCollageItems[i].mCursor = getCursor();
//                        mCollageItems[i].mItemBounds = childBounds;
//                        mCollageItems[i].mViewPosition = mPosition;
//                        mCollageItems[i].mCursorPosition = photoIndices.get(photoIndices.size() - i - 1);
//                        mCollageItems[i].mCollagePosition = i;
//                        mCollageItems[i].mTransitionInProgress = false;
//                        mCollageItems[i].mTransitionBitmaps[FROM] = null;
//
//                        if (i == 0) {
//                            loadItem(mCollageItems[0], false, true);
//                        } else {
//                            mCollageItems[i].mChain = true;
//                            loadNextItem(mCollageItems[i]);
//                        }
//                    }
//                }
//                invalidateFromThread();
//                startPhotoCycling();
//            } else {
//                Log.e("ZZLOAD", "loadItems cursor count is 0! cursor closed? " + mContentCursor.isClosed());
//            }
//        }
//    }
//
//    /**
//     * This is complicated and needs to be simplified. For now, here are the transitions that can occur
//     * and all the state variables required to make them work. Needs to be turned into a state machine.
//     *
//     *      Transition	        Chain	    UseLocal	TransitionType	TransitionFromCurrent	FadeInDuration
//     *      Empty to local	    TRUE	    TRUE	    CrossFade	    FALSE	                FADEIN_DURATION
//     *      Local to AgentLib	TRUE    	FALSE	    None	        FALSE	                0
//     *      Empty to AgentLib	TRUE	    FALSE	    CrossFade	    FALSE	                FADEIN_DURATION
//     *      PhotoCycle          FALSE       FALSE       CrossFade       TRUE	                CROSSFADE_DURATION
//     */
//    void setThumbnailForMobject(final CollageItem collageItem, final TransitionType transitionType) {
//        synchronized (mCollageItemsLock) {
//            if (collageItem != null && collageItem.mItem != null) {
//                transitionTo(collageItem, transitionType);
//                //loadNextItem(collageItem);
//                invalidateFromThread();
//            }
//        }
//    }
//
//    public void loadItem(final CollageItem collageItem, final boolean transitionFromCurrent, final boolean chain) {
//        loadItem(collageItem, transitionFromCurrent, chain, true);
//    }
//
//    public void loadNextItem(final CollageItem collageItem) {
//        if (collageItem.mChain && collageItem.mCollagePosition < mPanelCount) {
//            int index = getNextItemToLoad(collageItem.mCollagePosition);
//            if (index >= 0) {
//                loadItem(mCollageItems[index], false, true);
//            }
//        }
//    }
//
//    public int getNextItemToLoad(int startAt) {
//        int index = startAt;
//        int result = -1;
//
//        while (index < mPanelCount) {
//            CollageItem collageItem = mCollageItems[index];
//            if (collageItem.mItem == null || collageItem.mLoadInfo.getState() != LoadInfo.LoadState.Thumbnail) {
//                result = index;
//                break;
//            }
//            index++;
//        }
//
//        return result;
//    }
//
//    public void loadItem(final CollageItem collageItem, final boolean transitionFromCurrent, final boolean chain, final boolean useLocal) {
//        synchronized (mCollageItemsLock) {
//            Log.d("ZZLOAD", "loadItem: view: " + collageItem.mViewPosition + " pos: " + collageItem.mCursorPosition + " cursor: " +  mContentCursor + " isClosed: " + mContentCursor.isClosed());
//            ContentLoader.cancelThumbnailRequest(collageItem.mLoadInfo.getThumbnailRequestId());
//            collageItem.reset(transitionFromCurrent, chain, useLocal);
//            ContentLoader.requestMetaData(getCursorLock(), collageItem.mCursor, collageItem.mCursorPosition, collageItem.mLoadListener);
//        }
//    }
//
//    void invalidateFromThread() {
//        ((Activity)getContext()).runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                invalidate();
//            }
//        });
//    }
//
//    private Rect getChildBounds(int panelIndex, CollageLayout collageLayout, Rect layoutBounds) {
//        Rect childBounds = collageLayout.getPanelRect(panelIndex, layoutBounds);
//
//        childBounds.offset(layoutBounds.left, layoutBounds.top);
//
//        // Add the padding between the panels as well as the gap between collages.
//        adjustBottomRight(childBounds, layoutBounds);
//
//        return childBounds;
//    }
//
//    private void adjustBottomRight(final Rect childBounds, final Rect layoutBounds) {
//        int right = layoutBounds.width();
//        int bottom = layoutBounds.height();
//        int padding = LayoutUtils.getPanelPadding(layoutBounds.width());
//
//        if (childBounds.right < right) {
//            childBounds.right -= padding;
//        }
//
//        if (childBounds.bottom < bottom) {
//            childBounds.bottom -= padding;
//        }
//    }
//
//    public static List<Integer> distributeValues(final int first, final int max, final int numValues) {
//        ArrayList<Integer> values = new ArrayList<Integer>(numValues);
//        int last = max - 1;
//
//        if (numValues == 1) {
//            values.add(first);
//        } else if (numValues == 2) {
//            values.add(last);
//            values.add(first);
//        } else {
//            values.add(last);
//
//            int incrementBy = (last - first) / (numValues - 1);
//            for (int i = numValues - 2; i >= 1; i--) {
//                values.add(first + (i * incrementBy));
//            }
//
//            values.add(first);
//        }
//
//        return values;
//    }
//
//    private void closeCursor() {
//        if (mContentCursor != null) {
//            mContentCursor.close();
//            mContentCursorCount = 0;
//        }
//    }
//
//    @Override
//    public Cursor getCursor() {
//        return mContentCursor;
//    }
//
//    @Override
//    public void setCursor(Cursor cursor) {
//        //FIXME XXXJCC - Need to look into what should be done here.
//    }
//
//    @Override
//    public Object getCursorLock() {
//        return this;
//    }
//
//    @Override
//    public Query getQuery() {
//        return mQuery;
//    }
//
//    @Override
//    public ScrollPositions.ViewType getViewType() {
//        return null; //mAdapter.mViewType;
//    }
//
//    @Override
//    public boolean inSelectionMode() {
//        return false;
//    }
//
//    @Override
//    public void setSelectionMode(boolean on) {
//    }
//
//    @Override
//    public int findCursorPosition(ScrollPositions.ScrollPosition position, Mobject.FieldFlags itemField, int windowSize) {
//        return 0;  // Unused
//    }
//
//    @Override
//    public int findCursorPosition(ScrollPositions.ScrollPosition position, Mobject.DigestFields itemField, int windowSize) {
//        return 0;  // Unused
//    }
//
//    public int getCursorCount() {
//        return mContentCursorCount; // mContentCursor == null ? 0 : mContentCursor.getCount();
//    }
//
//    public long getAlbumTimestamp() {
//        return mAlbumTimestamp;
//    }
//
//    private void cyclePhoto() {
//        synchronized (mCollageItemsLock) {
//            if (getCursorCount() > mPanelCount) {
//                int randomCollagePanel = nextRandomChild();
//                mCollageItems[randomCollagePanel].mCursorPosition = nextRandomPhoto(mCollageItems[randomCollagePanel].mCursorPosition);
//                loadItem(mCollageItems[randomCollagePanel], true, false, false);
//            }
//        }
//    }
//
//    private HashSet<Integer> getNowShowing() {
//        HashSet<Integer> nowShowing = new HashSet<Integer>(mPanelCount);
//
//        for (int i = 0; i < mPanelCount; i++) {
//            CollageItem item = mCollageItems[i];
//            nowShowing.add(item.mCursorPosition);
//        }
//
//        return nowShowing;
//    }
//
//    private int nextRandomChild() {
//        int index;
//        int childCount = mPanelCount;
//        index = mRandom.nextInt(childCount - 1);
//        while (index == mLastPanel) {
//            index = (index + 1) % childCount;
//        }
//        mLastPanel = index;
//        return index;
//    }
//
//    private int nextRandomPhoto(int currentIndex) {
//        int index;
//        index = mRandom.nextInt(getCursorCount() - 1);
//        HashSet<Integer> nowShowing = getNowShowing();
//
//        while (nowShowing.contains(index)) {
//            index = (index + 1) % getCursorCount();
//        }
//        nowShowing.remove(currentIndex);
//        nowShowing.add(index);
//
//        return index;
//    }
//
//    private void startPhotoCycling() {
//        synchronized (mTimerLock) {
//            if (getCursorCount() > mPanelCount) {
//                if (mTimer != null) {
//                    mTimer.cancel();
//                }
//                mTimer = new Timer();
//                mTimer.schedule(new CyclePhotoTask(), ROTATE_FREQUENCY, ROTATE_FREQUENCY);
//            }
//        }
//    }
//
//    private void stopPhotoCycling() {
//        synchronized (mTimerLock) {
//            if (mTimer != null) {
//                mTimer.cancel();
//                mTimer = null;
//            }
//        }
//    }
//
//    public void dumpStatus() {
//        synchronized (mCollageItemsLock) {
//            Log.i("CollageView", "*************STATUS*************");
//            Log.i("CollageView", "Position: " + mPosition + " Panels: " + mPanelCount);
//            for (int i = 0; i < mPanelCount; i++) {
//                CollageItem item = mCollageItems[i];
//                Log.i("CollageView", "Panel: " + i);
//                Log.i("CollageView", "---Load state: " + item.mLoadInfo.getState());
//                ContentItem contentItem = item.mItem;
//                if (contentItem != null) {
//                    Log.i("CollageView", "---Catalog Id: " + contentItem.getCatalogId());
//                    Log.i("CollageView", "---Mobject Id: " + contentItem.getMobjectId());
//                    Log.i("CollageView", "---ThumbnailUriStatus: " + contentItem.getThumbnailUriStatus());
//                    Log.i("CollageView", "---ThumbnailUri: " + contentItem.getThumbnailUri());
//                    Log.i("CollageView", "---Thumbnail: " + contentItem.getThumbnail());
//                    Log.i("CollageView", "---Name: " + contentItem.getName());
//                }
//            }
//        }
//        Log.i("CollageView", "********************************");
//    }
//
//    class CyclePhotoTask extends TimerTask {
//        @Override
//        public void run() {
//            post(new CyclePhotoRunnable(CollageView.this));
//        }
//    }
//
//    static class CyclePhotoRunnable implements Runnable {
//        private WeakReference<CollageView> mCollageView;
//
//        public CyclePhotoRunnable(CollageView collageView) {
//            mCollageView = new WeakReference<CollageView>(collageView);
//        }
//
//        @Override
//        public void run() {
//            try {
//                CollageView collageView = mCollageView.get();
//                if (collageView != null) {
//                    collageView.cyclePhoto();
//                }
//            } catch (Exception ex) {
//                // We don't actually care if this fails occasionally
//                Log.i("CollageView", "rotatePhoto failed");
//                ex.printStackTrace();
//            }
//        }
//    }

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
//            FontUtils.Family family = LayoutUtils.getCollageInfoFont();

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

//    class ThumbnailLoadListener implements LoadListener {
//        private final CollageItem mCollageItem;
//
//        public ThumbnailLoadListener(CollageItem item) {
//            mCollageItem = item;
//        }
//
//        @Override
//        public void onMetaDataRequestQueued(long requestId) {
//            mCollageItem.mLoadInfo.setMetaDataRequestId(requestId);
//        }
//
//        @Override
//        public void onMetaDataLoaded(final long requestId, final ContentItem item) {
//            synchronized (mCollageItemsLock) {
//                ContentLoader.cancelLocalThumbnailRequest(mCollageItem.mLoadInfo.getLocalThumbnailRequestId());
//                ContentLoader.cancelThumbnailRequest(mCollageItem.mLoadInfo.getThumbnailRequestId());
//                Log.d("ZZLOAD", "onMetaDataLoaded: requestId: " + requestId + " view: " + mCollageItem.mViewPosition + " pos: " + mCollageItem.mCursorPosition + " item: " + item);
//                try {
//                    Long currentReqId = mCollageItem.mLoadInfo.getMetaDataRequestId();
//                    if ((currentReqId != null && currentReqId == requestId) && mCollageItem.mLoadInfo.getState() == LoadInfo.LoadState.Unknown) {
//                        mCollageItem.mLoadInfo.setState(LoadInfo.LoadState.MetaData);
//                        mCollageItem.mItem = item;
//                        if (mCollageItem.mUseLocal && item.getLocalId() != null) {
//                            ContentLoader.requestLocalThumbnail(item, this);
//                            Log.d("ZZLOAD", "onMetaDataLoaded: requesting local thumbnail requestId: " + requestId + " view: " + mCollageItem.mViewPosition + " pos: " + mCollageItem.mCursorPosition + " item: " + item);
//                        } else {
//                            ContentLoader.requestThumbnail(RequestPriority.Foreground, item, ThumbnailLoadListener.this);
//                            Log.d("ZZLOAD", "onMetaDataLoaded: requesting thumbnail requestId: " + mCollageItem.mLoadInfo.getThumbnailRequestId().toString() + " view: " + mCollageItem.mViewPosition + " pos: " + mCollageItem.mCursorPosition + " item: " + item);
//                        }
//                    }
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//
//        @Override
//        public void onLocalThumbnailRequestQueued(long requestId) {
//            mCollageItem.mLoadInfo.setLocalThumbnailRequestId(requestId);
//        }
//
//        @Override
//        public void onLocalThumbnailLoaded(final long requestId, final ContentItem item) {
//            synchronized (mCollageItemsLock) {
//                Log.d("ZZLOAD", "onLocalThumbnailLoaded: requestId: " + requestId + " view: " + mCollageItem.mViewPosition + " pos: " + mCollageItem.mCursorPosition + " item: " + item);
//                Long currentRequestId = mCollageItem.mLoadInfo.getLocalThumbnailRequestId();
//                if (currentRequestId != null && requestId == currentRequestId) {
//                    if (mCollageItem.mItem != null) {
//                        mCollageItem.mLoadInfo.setState(LoadInfo.LoadState.LocalThumbnail);
//                        if (item.getThumbnail() != null) {
//                            Log.d(TAG + "ZZLOAD", "onLocalThumbnailLoaded: showingThumbnail requestId: " + requestId + " view: " + mCollageItem.mViewPosition + " pos: " + mCollageItem.mCursorPosition + " item: " + item);
//
//                            setThumbnailForMobject(mCollageItem, TransitionType.Crossfade);
//                        }
//                        ContentLoader.requestThumbnail(RequestPriority.Foreground, item, ThumbnailLoadListener.this);
//                        Log.d(TAG + "ZZLOAD", "onLocalThumbnailLoaded: requesting thumbnail requestId: " + mCollageItem.mLoadInfo.getThumbnailRequestId().toString() + " view: " + mCollageItem.mViewPosition + " pos: " + mCollageItem.mCursorPosition + " item: " + item);
//                    }
//                } else {
//                    Log.d(TAG + "ZZLOAD", "onLocalThumbnailLoaded: skipping show due to wrong id requestId: " + requestId + " view: " + mCollageItem.mViewPosition + " pos: " + mCollageItem.mCursorPosition + " item: " + item);
//                }
//
//            }
//        }
//
//        @Override
//        public void onThumbnailRequestQueued(long requestId) {
//            mCollageItem.mLoadInfo.setThumbnailRequestId(requestId);
//        }
//
//        @Override
//        public void onThumbnailLoaded(Long requestId, Id alRequestId, ContentItem item, Status status, String remoteAgentId, long bytesRead, long bytesTotal) {
//            synchronized (mCollageItemsLock) {
//                Log.d(TAG + "ZZLOAD", "onThumbnailLoaded() requestId: " + requestId + " view: " + mCollageItem.mViewPosition + " pos: " + mCollageItem.mCursorPosition + " status: " + status.name() + " item: " + item);
//                if (mCollageItem.mLoadInfo != null && requestId.equals(mCollageItem.mLoadInfo.getThumbnailRequestId())) {
//                    Log.d(TAG + "ZZLOAD", "onThumbnailLoaded() showingThumbnail requestId: " + requestId + " view: " + mCollageItem.mViewPosition + " pos: " + mCollageItem.mCursorPosition + " status: " + status.name());
//                    TransitionType transitionType = TransitionType.None;
//                    if (status == Status.OK) {
//                        mCollageItem.mLoadInfo.setState(LoadInfo.LoadState.Thumbnail);
//                        transitionType = mCollageItem.mUseLocal && item.getThumbnail() != null ? TransitionType.None : TransitionType.Crossfade;
//                    }
//                    setThumbnailForMobject(mCollageItem, transitionType);
//                }
//            }
//        }
//
//        @Override
//        public void onBlobLoaded(Id requestId, ContentItem item, Status status, String remoteAgentId, long bytesRead, long bytesTotal) {
//        }
//    }
}
