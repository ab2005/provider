/*
 * Copyright (C) 2015 Seagate LLC
 */

package com.seagate.alto;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.util.Pair;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.seagate.alto.events.ItemSelectedEvent;

import java.util.ArrayList;

/**
 * Provides UI for the view with Tile.
 */
public class TileContentView extends RecyclerView {

    private static String TAG = LogUtils.makeTag(TileContentView.class);

    private ContentAdapter mAdapter;

    public TileContentView(Context context) {
        this(context, null, 0);
    }

    public TileContentView(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public TileContentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAdapter = new ContentAdapter();
        mAdapter.setActivity((Activity) getContext());
        setAdapter(mAdapter);
        setHasFixedSize(true);
        setLayoutManager(new LinearLayoutManager(getContext()));

        int tilePadding = getResources().getDimensionPixelSize(R.dimen.tile_padding);
        this.setPadding(tilePadding, tilePadding, tilePadding, tilePadding);
        this.setLayoutManager(new GridLayoutManager(getContext(), 2));

    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView drawee;
        int position;

        public ViewHolder(LayoutInflater inflater, final ViewGroup parent, final Activity activity) {

            super(inflater.inflate(R.layout.item_tile, parent, false));

            drawee = (SimpleDraweeView) itemView.findViewById(R.id.drawee);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                if (parent.getContext() instanceof MainActivity) {

                    ArrayList<Pair<View, String>> pairs = new ArrayList<Pair<View, String>>();
                    Pair<View, String> imagePair = Pair.create((View) drawee, "tThumbnail");
                    pairs.add(imagePair);

                    BusMaster.getBus().post(new ItemSelectedEvent(position, pairs));

                }
                }
            });
        }
    }

    /**
     * Adapter to display recycler view.
     */
    public static class ContentAdapter extends RecyclerView.Adapter<ViewHolder> {

        private Activity mActivity;

        public ContentAdapter() {
            // no-op
        }

        public void setActivity(Activity mActivity) {
            this.mActivity = mActivity;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent, mActivity);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

            Uri uri = PlaceholderContent.getUri(position);
            holder.drawee.setImageURI(uri);

            Log.d("seagate-tile", "uri=" + uri);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.drawee.setTransitionName("TileThumb"+position);
            }

            holder.position = position;
        }

        @Override
        public int getItemCount() {
            return PlaceholderContent.getCount();
        }
    }
}