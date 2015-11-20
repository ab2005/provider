/*
 * Copyright (C) 2015 Seagate LLC
 */

package com.seagate.alto;

import android.app.Activity;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.TransitionInflater;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;

/**
 * Provides UI for the view with List.
 */
public class ListContentFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, container, false);
        ContentAdapter adapter = new ContentAdapter();
        adapter.setActivity(getActivity());
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return recyclerView;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView drawee;
        int position;

        public ViewHolder(LayoutInflater inflater, final ViewGroup parent, final Activity activity) {
            super(inflater.inflate(R.layout.item_list, parent, false));

            drawee = (SimpleDraweeView) itemView.findViewById(R.id.list_avatar);

            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            drawee.getHierarchy().setRoundingParams(roundingParams);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                if (parent.getContext() instanceof MainActivity) {

                    MainActivity main = (MainActivity) parent.getContext();

                    Fragment details = new DetailFragment();

                    Bundle args = new Bundle();
                    args.putInt(PlaceholderContent.INDEX, position);
                    details.setArguments(args);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        details.setSharedElementEnterTransition(TransitionInflater.from(parent.getContext()).inflateTransition(R.transition.trans_move));
                        details.setSharedElementReturnTransition(TransitionInflater.from(parent.getContext()).inflateTransition(R.transition.trans_move));
                    }

                    ArrayList<Pair<View, String>> pairs = new ArrayList<Pair<View, String>>();
                    Pair<View, String> imagePair = Pair.create((View) drawee, "tThumbnail");
                    pairs.add(imagePair);

                    main.pushFragment(details, pairs);
                }

                }
            });
        }
    }

    /**
     * Adapter to display recycler view.
     */
    public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {

        public void setActivity(Activity mActivity) {
            this.mActivity = mActivity;
        }

        private Activity mActivity;

        public ContentAdapter() {
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent, mActivity);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Uri uri = PlaceholderContent.getUri(position);
            holder.drawee.setImageURI(uri);

            Log.d("seagate-list", "uri=" + uri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.drawee.setTransitionName("ListThumb"+position);
            }

            holder.position = position;
        }

        @Override
        public int getItemCount() {
            return PlaceholderContent.getCount();
        }
    }

}

