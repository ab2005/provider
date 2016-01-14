package com.seagate.alto.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.seagate.alto.R;

public class DigestListView extends RecyclerView {

    private static String TAG = DigestListView.class.getName();
    private ContentAdapter mAdapter;

    public DigestListView(Context context) {
        super(context);
    }

    public DigestListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DigestListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        mAdapter = new ContentAdapter();
        setAdapter(mAdapter);
        setHasFixedSize(true);
        setLayoutManager(new LinearLayoutManager(getContext()));
    }

    public static class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.DigestCellViewHolder> {

        public static class DigestCellViewHolder extends RecyclerView.ViewHolder {
            DigestCellView digestCellView;

            public DigestCellViewHolder(LayoutInflater inflater, final ViewGroup parent) {
                super(inflater.inflate(R.layout.digest_view_cell, parent, false));
                digestCellView = (DigestCellView) itemView.findViewById(R.id.group);
            }
        }

        public ContentAdapter() {

        }

        @Override
        public ContentAdapter.DigestCellViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder");
            DigestCellViewHolder viewHolder = new DigestCellViewHolder(LayoutInflater.from(parent.getContext()), parent);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(DigestCellViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder()");
            // based on position, we should be able to get the date of images in this cell
            // Date date = getDateFromPosition(position);
            holder.digestCellView.loadContent(position);
        }

        @Override
        public int getItemCount() {
            return 10;
        }

    }
}
