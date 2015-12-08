package com.seagate.alto;

// add a class header comment here

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.seagate.alto.events.ItemSelectedEvent;
import com.squareup.otto.Subscribe;

public class DetailFragment extends Fragment {

    private static String TAG = LogUtils.makeTag(DetailFragment.class);

    private View mView;

    public DetailFragment() {
//        BusMaster.getBus().register(this);
        Log.d(TAG, "constructor");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
//        BusMaster.getBus().unregister(this);
        Log.d(TAG, "finalize");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

//        BusMaster.getBus().register(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
            setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
        }

        mView = inflater.inflate(R.layout.fragment_detail, container, false);

        int index = 0;
        Bundle args = getArguments();
        if (args != null) {
            index = args.getInt(PlaceholderContent.INDEX);
        }

        showItem(index);

        return mView;
    }

    private void showItem(int index) {
        if (index >= 0 && mView != null) {
            SimpleDraweeView sdv = (SimpleDraweeView) mView.findViewById(R.id.image);
            sdv.setImageURI(PlaceholderContent.getUri(index));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        BusMaster.getBus().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        BusMaster.getBus().unregister(this);
    }

    @Subscribe
    public void answerAvailable(ItemSelectedEvent event) {
        // TODO: React to the event somehow!
        Log.d(TAG, "item selected: " + event.getPosition());

        showItem(event.getPosition());

    }

}
