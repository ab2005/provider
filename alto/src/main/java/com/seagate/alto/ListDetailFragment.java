// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

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

import com.seagate.alto.events.BusMaster;
import com.seagate.alto.events.ItemSelectedEvent;
import com.seagate.alto.utils.LogUtils;
import com.squareup.otto.Subscribe;

public class ListDetailFragment extends Fragment implements IBackStackName {

    private String TAG = makeTag();

    protected String makeTag() {
        return LogUtils.makeTag(ListDetailFragment.class);
    }

    protected DetailView mDetail;

    public ListDetailFragment() {
        Log.d(TAG, "constructor");
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        Log.d(TAG, "finalize");
    }

    protected int getLayout() {
        return R.layout.split;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
//            setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
//        }


        View v = inflater.inflate(getLayout(), container, false);

        mDetail = (DetailView) v.findViewById(R.id.detail);

        if (mDetail != null) {

            int index = 0;
            Bundle args = getArguments();
            if (args != null) {
                index = args.getInt(PlaceholderContent.INDEX);
            }

            mDetail.showItem(index);
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored");
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

        handleItemSelected(event);

    }

    protected void handleItemSelected(ItemSelectedEvent event) {
        if (mDetail == null) {
            if (getParentFragment() instanceof IFragmentStackHolder) {

                IFragmentStackHolder fsh = (IFragmentStackHolder) getParentFragment();

                Fragment details = new DetailListFragment();

                Bundle args = new Bundle();
                args.putInt(PlaceholderContent.INDEX, event.getPosition());
                details.setArguments(args);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    details.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
                    details.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
                }

                fsh.pushFragment(details, event.getPairs());

            }
        }
    }

    @Override
    public String getBackStackName() {
        return "list-detail:w600dp";
    }
}
