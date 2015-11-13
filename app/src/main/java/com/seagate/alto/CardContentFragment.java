/*
 * Copyright (C) 2015 Seagate LLC
 */

package com.seagate.alto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.android.alto.R;
import com.facebook.drawee.view.SimpleDraweeView;

/**
 * Provides UI for the view with Cards.
 */
public class CardContentFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
                R.layout.recycler_view, container, false);
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

        public ViewHolder(LayoutInflater inflater, ViewGroup parent, final Activity activity) {
            super(inflater.inflate(R.layout.item_card, parent, false));

            drawee = (SimpleDraweeView) itemView.findViewById(R.id.card_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Context context = v.getContext();
                    Intent intent = new Intent(context, DetailActivity.class);
                    intent.putExtra(PlaceholderContent.INDEX, position);

                    // Set up the transition
//                    View name = v.findViewById(R.id.name_holder);
                    Pair<View, String> imagePair = Pair.create((View)drawee, "tThumbnail");
//                    Pair<View, String> namePair = Pair.create(name, "tName");
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity,
                            imagePair);

                    ActivityCompat.startActivity(activity, intent, options.toBundle());
//                    context.startActivity(intent);
                }
            });

            // Adding Snackbar to Action Button inside card
            Button button = (Button)itemView.findViewById(R.id.action_button);
            button.setOnClickListener(new View.OnClickListener(){
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

        // FIXME - Not happy with having to store this and pass it around.  Need to look closer at need here.
        private FragmentActivity mActivity;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent, mActivity);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Uri uri = PlaceholderContent.getUri(position);
            holder.drawee.setImageURI(uri);
            holder.position = position;
        }

        @Override
        public int getItemCount() {
            return PlaceholderContent.getCount();
        }

        public void setActivity(FragmentActivity activity) {
            this.mActivity = activity;
        }
    }
}
