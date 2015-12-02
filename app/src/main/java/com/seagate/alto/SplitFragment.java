package com.seagate.alto;

// add a class header comment here

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.otto.Subscribe;

import java.util.ArrayList;

public class SplitFragment extends Fragment {

    private static String TAG = LogUtils.makeTag(SplitFragment.class);

    DetailView mDetail;
//    DetailView mDetail2;
//    View mImage;

    public SplitFragment() {
        Log.d(TAG, "constructor");
//        BusMaster.getBus().register(this);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d(TAG, "finalize");
//        BusMaster.getBus().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
//            setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
//        }

        View v = inflater.inflate(R.layout.split, container, false);

        mDetail = (DetailView) v.findViewById(R.id.detail);
//        mDetail2 = (DetailView) v.findViewById(R.id.detail_view);

//        mImage = v.findViewById(R.id.image);

//        Fragment f =

//        int index = -1;
//        Bundle args = getArguments();
//        if (args != null) {
//            index = args.getInt(PlaceholderContent.INDEX);
//        }
//
//        if (index >= 0) {
//            SimpleDraweeView sdv = (SimpleDraweeView) v.findViewById(R.id.image);
//            sdv.setImageURI(PlaceholderContent.getUri(index));
//        }

        return v;
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

        if (mDetail == null) {
            if (getActivity() instanceof MainActivity) {

                MainActivity main = (MainActivity) getActivity();

                Fragment details = new DetailFragment();

                Bundle args = new Bundle();
                args.putInt(PlaceholderContent.INDEX, event.getPosition());
                details.setArguments(args);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    details.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
                    details.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
                }

                ArrayList<Pair<View, String>> pairs = new ArrayList<Pair<View, String>>();
//                Pair<View, String> imagePair = Pair.create((View) drawee, "tThumbnail");
//                pairs.add(imagePair);

                main.pushFragment(details, pairs);

            }
        }

        // if both fragments are here, then do nothing
        // else launch the detail fragment


//        showItem(event.getPosition());

    }

}
