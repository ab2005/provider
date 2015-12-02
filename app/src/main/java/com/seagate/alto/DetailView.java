package com.seagate.alto;

// add a class header comment here

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.otto.Subscribe;

public class DetailView extends android.support.v4.widget.NestedScrollView {

    private static String TAG = LogUtils.makeTag(DetailView.class);

    private View mView;

    public DetailView(Context context) {
        this(context, null, 0);
    }

    public DetailView(Context context, AttributeSet attrs) {
        this(context, null, 0);
    }

    public DetailView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        Log.d(TAG, "constructor");
    }

//    public DetailView() {
////        BusMaster.getBus().register(this);
//        Log.d(TAG, "constructor");
//    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
//        BusMaster.getBus().unregister(this);
        Log.d(TAG, "finalize");
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//
//        Log.d(TAG, "onCreateView");
//
////        BusMaster.getBus().register(this);
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
//            setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
//        }
//
//        mView = inflater.inflate(R.layout.fragment_detail, container, false);
//
//        int index = 0;
//        Bundle args = getArguments();
//        if (args != null) {
//            index = args.getInt(PlaceholderContent.INDEX);
//        }
//
//        showItem(index);
//
//        return mView;
//    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d(TAG, "onFinishInflate");
        BusMaster.getBus().register(this);

        showItem(0);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");
        BusMaster.getBus().unregister(this);
    }

    private void showItem(int index) {

        SimpleDraweeView sdv = (SimpleDraweeView) findViewById(R.id.image);
        if (sdv != null) {
            sdv.setImageURI(PlaceholderContent.getUri(index));
        }

    }

//    @Override
//    public void onResume() {
//        super.onResume();
//        Log.d(TAG, "onResume");
//        BusMaster.getBus().register(this);
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//        Log.d(TAG, "onPause");
//        BusMaster.getBus().unregister(this);
//    }

    @Subscribe
    public void answerAvailable(ItemSelectedEvent event) {
        // TODO: React to the event somehow!
        Log.d(TAG, "item selected: " + event.getPosition());

        showItem(event.getPosition());

    }

}
