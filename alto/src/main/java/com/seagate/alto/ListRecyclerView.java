// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.seagate.alto.events.BusMaster;
import com.seagate.alto.events.ItemSelectedEvent;
import com.seagate.alto.utils.LogUtils;
import com.seagate.alto.utils.SharingUtils;

import java.util.ArrayList;

/**
 * Provides UI for the view with List.
 */
public class ListRecyclerView extends RecyclerView {

    private static String TAG = LogUtils.makeTag(ListRecyclerView.class);

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
        setAdapter(mAdapter);
        setHasFixedSize(true);
        setLayoutManager(new LinearLayoutManager(getContext()));

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow");

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView drawee;
        TextView title;
        int position;

        public ViewHolder(LayoutInflater inflater, final ViewGroup parent) {
            super(inflater.inflate(R.layout.item_list, parent, false));

            drawee = (SimpleDraweeView) itemView.findViewById(R.id.list_avatar);
            title = (TextView) itemView.findViewById(R.id.list_title);

            RoundingParams roundingParams = RoundingParams.fromCornersRadius(5f);
            roundingParams.setRoundAsCircle(true);
            drawee.getHierarchy().setRoundingParams(roundingParams);

            itemView.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {

                                                ArrayList<Pair<View, String>> pairs = new ArrayList<Pair<View, String>>();
                                                Pair<View, String> imagePair = Pair.create((View) drawee, "tThumbnail");
                                                pairs.add(imagePair);
                                                Pair<View, String> titlePair = Pair.create((View) title, "tTitle");
                                                pairs.add(titlePair);

                                                BusMaster.getBus().post(new ItemSelectedEvent(position, pairs));

                                            }
                                        }
            );

            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.add("share").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            SharingUtils.shareImageFromRecyclerView(position, itemView.getContext());
                            return true;
                        }
                    });
                }
            });

        }

        /**
         * Adapter to display recycler view.
         */
        public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {

            @Override
            public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
            }

            @Override
            public void onBindViewHolder(ViewHolder holder, int position) {
                Uri uri = PlaceholderContent.getThumbnailUri(position, 128);
                holder.drawee.setImageURI(uri);

                Log.d(TAG, "uri=" + uri);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.drawee.setTransitionName("ListThumb" + position);
                    holder.title.setTransitionName("ListName" + position);
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
