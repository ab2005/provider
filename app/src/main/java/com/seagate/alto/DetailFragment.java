package com.seagate.alto;

// add a class header comment here

import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.transition.TransitionInflater;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;

public class DetailFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setSharedElementEnterTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
            setSharedElementReturnTransition(TransitionInflater.from(getActivity()).inflateTransition(R.transition.trans_move));
        }

        View v = inflater.inflate(R.layout.fragment_detail, container, false);

        int index = -1;
        Bundle args = getArguments();
        if (args != null) {
            index = args.getInt(PlaceholderContent.INDEX);
        }

        if (index >= 0) {
            SimpleDraweeView sdv = (SimpleDraweeView) v.findViewById(R.id.image);
            sdv.setImageURI(PlaceholderContent.getUri(index));
        }

        return v;
    }

}
