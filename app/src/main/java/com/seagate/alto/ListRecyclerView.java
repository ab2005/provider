//  copyright (c) 2015. seagate technology plc. all rights reserved.

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
import android.widget.TextView;

import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.seagate.alto.events.BusMaster;
import com.seagate.alto.events.ItemSelectedEvent;
import com.seagate.alto.utils.LogUtils;

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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        SimpleDraweeView drawee;
        TextView title;
        int position;

        public ViewHolder(LayoutInflater inflater, final ViewGroup parent, final Activity activity) {
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
