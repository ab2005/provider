package com.seagate.alto;

// add a class header comment here

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.TransitionInflater;
import android.util.Log;

import com.seagate.alto.events.ItemSelectedEvent;
import com.seagate.alto.utils.LogUtils;
import com.squareup.otto.Subscribe;

public class CardDetailFragment extends ListDetailFragment implements IBackStackName {

    private String TAG = makeTag();

    protected String makeTag() {
        return LogUtils.makeTag(CardDetailFragment.class);
    }

    protected int getLayout() {
        return R.layout.card_detail;
    }

    @Override
    public String getBackStackName() {
        return "card-detail:w600dp";
    }

    // each class must subscribe to the event

    @Subscribe
    public void answerAvailable(ItemSelectedEvent event) {

        Log.d(TAG, "item selected: " + event.getPosition());

        if (mDetail == null) {
            if (getActivity() instanceof IFragmentStackHolder) {

                IFragmentStackHolder fsh = (IFragmentStackHolder) getActivity();

                Fragment details = new DetailCardFragment();

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
}
