/*
 * Copyright (C) 2015 Seagate LLC
 */

package com.seagate.alto;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.seagate.alto.events.ItemSelectedEvent;

import java.util.ArrayList;

/**
 * Provides UI for the view with List.
 */
public class ListRecyclerView extends RecyclerView {

    private static String TAG = LogUtils.makeTag(ListRecyclerView.class);

//    public ListContentView() {
////        super.Recycler()
////        BusMaster.getBus().register(this);
//        Log.d(TAG, "constructor");
//    }

//    @Override
//    protected void finalize() throws Throwable {
//        super.finalize();
////        BusMaster.getBus().unregister(this);
//        Log.d(TAG, "finalize");
//    }

    ViewHolder.ContentAdapter mAdapter;

    private int mSelection = 0;

    public ListRecyclerView(Context context) {
        this(context, null, 0);
    }

    public ListRecyclerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ListRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d(TAG, "constructor");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        Log.d(TAG, "onFinishInflate");

        mAdapter = new ViewHolder.ContentAdapter();
        mAdapter.setActivity((Activity) getContext());
        setAdapter(mAdapter);
        setHasFixedSize(true);
        setLayoutManager(new LinearLayoutManager(getContext()));

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");

    }

    //    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//
////        BusMaster.getBus().register(this);
//
//        RecyclerView recyclerView = (RecyclerView) inflater.inflate(R.layout.recycler_view, container, false);
//        mAdapter = new ViewHolder.ContentAdapter();
//        mAdapter.setActivity(getActivity());
//        recyclerView.setAdapter(mAdapter);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        return recyclerView;
//    }

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

                        ArrayList<Pair<View, String>> pairs = new ArrayList<Pair<View, String>>();
                        Pair<View, String> imagePair = Pair.create((View) drawee, "tThumbnail");
                        pairs.add(imagePair);


                        BusMaster.getBus().post(new ItemSelectedEvent(position, pairs));

//                                                if (parent.getContext() instanceof MainActivity) {
//
//                                                    MainActivity main = (MainActivity) parent.getContext();
//
//                                                    if (main.getTwoPane()) {
//                                                        BusMaster.getBus().post(new ItemSelectedEvent(position));
//                                                    } else {
//                                                        Fragment details = new DetailFragment();
//
//                                                        Bundle args = new Bundle();
//                                                        args.putInt(PlaceholderContent.INDEX, position);
//                                                        details.setArguments(args);
//
//                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                                                            details.setSharedElementEnterTransition(TransitionInflater.from(parent.getContext()).inflateTransition(R.transition.trans_move));
//                                                            details.setSharedElementReturnTransition(TransitionInflater.from(parent.getContext()).inflateTransition(R.transition.trans_move));
//                                                        }
//
//                                                        ArrayList<Pair<View, String>> pairs = new ArrayList<Pair<View, String>>();
//                                                        Pair<View, String> imagePair = Pair.create((View) drawee, "tThumbnail");
//                                                        pairs.add(imagePair);
//
//                                                        main.pushFragment(details, pairs);
//                                                    }
//
//                                                }
                    }
                }
            );
        }
//    }

//    @Produce
//    public ItemSelectedEvent produceAnswer() {
//        // Assuming 'lastAnswer' exists.
//
//        Log.d(TAG, "ItemSelectedEvent produceAnswer = " + mSelection);
//
//        return new ItemSelectedEvent(mSelection);
//    }

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

                Log.d(TAG, "uri=" + uri);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.drawee.setTransitionName("ListThumb" + position);
                }

                holder.position = position;
            }

            @Override
            public int getItemCount() {
                return PlaceholderContent.getCount();
            }
        }


    }
}
