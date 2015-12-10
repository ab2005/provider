//  copyright (c) 2015. seagate technology plc. all rights reserved.

package com.seagate.alto;

// add a class header comment here

import com.seagate.alto.utils.LogUtils;

public class DetailCardFragment extends ListDetailFragment implements IBackStackName {

    private String TAG = makeTag();

    protected String makeTag() {
        return LogUtils.makeTag(DetailCardFragment.class);
    }

    protected int getLayout() {
        return R.layout.detail_card;
    }

    @Override
    public String getBackStackName() {
        return "card-detail:w600dp";
    }

    // each class must subscribe to the event

//    @Subscribe
//    public void answerAvailable(ItemSelectedEvent event) {
//
//        Log.d(TAG, "item selected: " + event.getPosition());
//
//        if (mDetail == null) {
//            if (getActivity() instanceof MainActivity) {
//
//                MainActivity main = (MainActivity) getActivity();
//
//                Fragment details = new DetailTileFragment();
//
//                Bundle args = new Bundle();
//                args.putInt(PlaceholderContent.INDEX, event.getPosition());
//                details.setArguments(args);
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                    details.setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
//                    details.setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
//                }
//
//                main.pushFragment(details, event.getPairs());
//
//            }
//        }
//
//    }
}
