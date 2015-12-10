//  copyright (c) 2015. seagate technology plc. all rights reserved.

package com.seagate.alto;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.facebook.drawee.view.SimpleDraweeView;
import com.seagate.alto.events.BusMaster;
import com.seagate.alto.events.ItemSelectedEvent;
import com.seagate.alto.utils.LogUtils;

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
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAdapter = new ContentAdapter();
        mAdapter.setActivity((FragmentActivity) getContext());
        setAdapter(mAdapter);
        setHasFixedSize(true);
        setLayoutManager(new LinearLayoutManager(getContext()));

    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        RecyclerView recyclerView = (RecyclerView) inflater.inflate(
//                R.layout.recycler_view, container, false);
//        ContentAdapter adapter = new ContentAdapter();
//        adapter.setActivity(getActivity());
//        recyclerView.setAdapter(adapter);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        return recyclerView;
//    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView drawee;
        int position;

        public ViewHolder(LayoutInflater inflater, final ViewGroup parent, final Activity activity) {
            super(inflater.inflate(R.layout.bill_card, parent, false));

            drawee = (SimpleDraweeView) itemView.findViewById(R.id.card_image);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (parent.getContext() instanceof MainActivity) {

//                        MainActivity main = (MainActivity) parent.getContext();
//
//                        Fragment details = new DetailFragment();
//
//                        Bundle args = new Bundle();
//                        args.putInt(PlaceholderContent.INDEX, position);
//                        details.setArguments(args);
//
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                            details.setSharedElementEnterTransition(TransitionInflater.from(parent.getContext()).inflateTransition(R.transition.trans_move));
//                            details.setSharedElementReturnTransition(TransitionInflater.from(parent.getContext()).inflateTransition(R.transition.trans_move));
//                        }
//
                        ArrayList<Pair<View, String>> pairs = new ArrayList<Pair<View, String>>();
                        Pair<View, String> imagePair = Pair.create((View) drawee, "tThumbnail");
                        pairs.add(imagePair);

                        BusMaster.getBus().post(new ItemSelectedEvent(position, pairs));

//
//                        main.pushFragment(details, pairs);
                    }
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
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.drawee.setTransitionName("CardThumb"+position);
            }

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
