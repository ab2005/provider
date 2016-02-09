// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.seagate.alto.events.BusMaster;
import com.seagate.alto.events.ItemSelectedEvent;
import com.seagate.alto.utils.LogUtils;
import com.seagate.alto.utils.SharingUtils;

import java.util.ArrayList;

/**
 * Provides UI for the view with Cards.
 */
public class CardContentView extends RecyclerView {

    private static String TAG = LogUtils.makeTag(CardContentView.class);

    private ContentAdapter mAdapter;

    public CardContentView(Context context) {
        this(context, null, 0);
    }

    public CardContentView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CardContentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        Log.d(TAG, "constructor");
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAdapter = new ContentAdapter();
        setAdapter(mAdapter);
        setHasFixedSize(true);
        setLayoutManager(new LinearLayoutManager(getContext()));

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView drawee;
        int position;
        TextView title;

        public ViewHolder(LayoutInflater inflater, final ViewGroup parent) {
            super(inflater.inflate(R.layout.bill_card, parent, false));

            drawee = (SimpleDraweeView) itemView.findViewById(R.id.card_image);
            title = (TextView) itemView.findViewById(R.id.card_title);

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
            });

            itemView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
                @Override
                public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
                    menu.add("share").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            SharingUtils.shareImageFromRecyclerView(position, (Activity) itemView.getContext());
                            return true;
                        }
                    });
                }
            });

            // Adding Snackbar to Action Button inside card
            Button button = (Button)itemView.findViewById(R.id.action_button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, "Action is pressed",
                            Snackbar.LENGTH_LONG).show();
                }
            });

            ImageButton favoriteImageButton =
                    (ImageButton) itemView.findViewById(R.id.favorite_button);
            favoriteImageButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, "Added to Favorite",
                            Snackbar.LENGTH_LONG).show();
                }
            });

            ImageButton shareImageButton = (ImageButton) itemView.findViewById(R.id.share_button);
            shareImageButton.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    Snackbar.make(v, "Share article",
                            Snackbar.LENGTH_LONG).show();
                }
            });
        }
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
            Uri uri = PlaceholderContent.getThumbnailUri(position);
            holder.drawee.setImageURI(uri);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.drawee.setTransitionName("CardThumb"+position);
                holder.title.setTransitionName("CardTitle" + position);
            }

            holder.position = position;
        }

        @Override
        public int getItemCount() {
            return PlaceholderContent.getCount();
        }
    }
}
