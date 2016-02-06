package com.seagate.alto.ui;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.FadeDrawable;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeHolder;
import com.facebook.drawee.view.MultiDraweeHolder;
import com.seagate.alto.PlaceholderContent;
import com.seagate.alto.utils.LayoutUtils;
import com.seagate.alto.utils.LogUtils;
import com.seagate.alto.utils.ScreenUtils;

import java.util.Random;

public class ImageSwitchView extends ImageView {

    private final static String TAG = LogUtils.makeTag(ImageSwitchView.class);
    enum TransitionType {None, Crossfade, FlipHorizontal, FlipVertical}

    private MultiDraweeHolder<GenericDraweeHierarchy> mMultiDraweeHolder;
    private FadeDrawable mFadeDrawable;
    private int mCurrentIndex;
    private Handler mImageLoopingHandler = new Handler();

    private Random mRandom = new Random();

    private final static int CROSSFADE_DURATION = 2000;
    private static final int FADEIN_DURATION = 800;
    private static final int FLIP_DURATION = 250;
    private static final int ROTATE_FREQUENCY = 10000; // ms

    public ImageSwitchView(Context context) {
        super(context);
        init(context);
    }

    public ImageSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
        init(context);
    }

    public ImageSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Log.d(TAG, "init()");

        mMultiDraweeHolder = new MultiDraweeHolder<GenericDraweeHierarchy>();
        // holders for the two images to cross-fade between
        Drawable drawables[] = new Drawable[2];
        for (int i = 0; i < drawables.length; i++) {
            GenericDraweeHierarchy hierarchy = createDraweeHierarchy();
            mMultiDraweeHolder.add(DraweeHolder.create(hierarchy, getContext()));
            drawables[i] = hierarchy.getTopLevelDrawable();
            drawables[i].setCallback(this);
        }
        mFadeDrawable = new FadeDrawable(drawables);
        // no need to override onDraw, ImageView superclass will correctly draw our fade drawable if we set it like this:
        super.setImageDrawable(mFadeDrawable);
        mCurrentIndex = 0;
    }

    public void loadContent(DraweeController controller) {
        Log.d(TAG, "ImageSwitchView size: " + this.getWidth() + "/" + this.getHeight());
//        Log.d(TAG, "loadContent(): " + controller.getHierarchy().getTopLevelDrawable());
        mMultiDraweeHolder.get(mCurrentIndex).setController(controller);
    }

    public DraweeHolder<GenericDraweeHierarchy> getCurrentDrawee() {
        return mMultiDraweeHolder.get(mCurrentIndex);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // gonna be called when rotating the screen. Recalculate the layouts/bounds/sizes here.
        Log.d(TAG, "onMeasure()");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size;
        int realSize;

        DigestCellView parent = (DigestCellView) getParent();
        size = parent.getWidth();
        int margin = LayoutUtils.getDigestMargin(size);
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) getLayoutParams();
        lp.leftMargin = margin;
        lp.topMargin = margin;
        lp.rightMargin = ScreenUtils.isPortrait()? margin : 0;
        lp.bottomMargin = ScreenUtils.isPortrait()? 0 : margin;
        setLayoutParams(lp);

        realSize = size - 2 * margin;
        setMeasuredDimension(realSize, realSize);
        Log.d(TAG, "onMeasure: realSize: " + realSize);
        invalidate();
    }

    @Override
    public void onAttachedToWindow() {
        Log.d(TAG, "onAttachedToWindow()");
        super.onAttachedToWindow();
        attachDraweeHolders();

        resumeSwitching();
    }

    @Override
    public void onDetachedFromWindow() {
        Log.d(TAG, "onDetachedFromWindow()");
        super.onDetachedFromWindow();
        detachDraweeHolders();

        pauseSwitching();
    }

    @Override
    public void onStartTemporaryDetach() {
        Log.d(TAG, "onStartTemporaryDetach()");
        super.onStartTemporaryDetach();
        detachDraweeHolders();
    }

    @Override
    public void onFinishTemporaryDetach() {
        Log.d(TAG, "onFinishTemporaryDetach()");
        super.onFinishTemporaryDetach();
        attachDraweeHolders();
    }

    private void detachDraweeHolders() {
        for (int i = 0; i < mMultiDraweeHolder.size(); i++) {
            mMultiDraweeHolder.get(i).onDetach();
        }
    }

    private void attachDraweeHolders() {
        for (int i = 0; i < mMultiDraweeHolder.size(); i++) {
            mMultiDraweeHolder.get(i).onAttach();
        }
    }

    public void resumeSwitching() {
        Log.d(TAG, "resumeSwitching()");
        int startingTime = mRandom.nextInt(3 * ROTATE_FREQUENCY);
        mImageLoopingHandler.postDelayed(doImageTransition, startingTime);
    }

    public void pauseSwitching() {
        Log.d(TAG, "pauseSwitching()");
        mImageLoopingHandler.removeCallbacks(doImageTransition);
    }

    @Override
    protected boolean verifyDrawable(Drawable who) {
        super.verifyDrawable(who);
        Log.d(TAG, "verifyDrawable()");
        for (int i = 0; i < mMultiDraweeHolder.size(); i++) {
            if (who == mMultiDraweeHolder.get(i).getTopLevelDrawable()) {
                return true;
            }
        }
        return false;
    }

    private GenericDraweeHierarchy createDraweeHierarchy() {
        GenericDraweeHierarchy hierarchy = new GenericDraweeHierarchyBuilder(getResources())
                .setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                .setFadeDuration(10)
                .build();
        return hierarchy;
    }

    public Drawable getCurrentDrawable() {
        return mFadeDrawable.getDrawable(mCurrentIndex);
    }

    public void setBounds(Rect bounds) {
        this.layout(bounds.left, bounds.top, bounds.right, bounds.bottom);
    }

    private Runnable doImageTransition = new Runnable() {
        @Override
        public void run() {
//            Log.d(TAG, "doImageTransition runnable...");
            mCurrentIndex = (mCurrentIndex + 1) % 2;
            DraweeHolder<GenericDraweeHierarchy> drawee = mMultiDraweeHolder.get(mCurrentIndex);
            int position = (int)(PlaceholderContent.getCount() * Math.random());
            Uri uri = PlaceholderContent.getThumbnailUri(position);
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setOldController(drawee.getController())
                    .setUri(uri)
                    .build();
            drawee.setController(controller);
            mFadeDrawable.setTransitionDuration(CROSSFADE_DURATION);
            mFadeDrawable.fadeToLayer(mCurrentIndex);
            mImageLoopingHandler.postDelayed(this, ROTATE_FREQUENCY);
        }
    };
}
