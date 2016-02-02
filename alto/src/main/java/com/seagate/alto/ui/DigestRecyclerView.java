// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.ui;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.seagate.alto.R;
import com.seagate.alto.utils.LogUtils;

import java.util.ArrayList;

public class DigestRecyclerView extends RecyclerView {

    private final static String TAG = LogUtils.makeTag(DigestRecyclerView.class);
    private ContentAdapter mAdapter;

    public DigestRecyclerView(Context context) {
        super(context);
    }

    public DigestRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DigestRecyclerView(Context context, AttributeSet attrs, int defStyle) {
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

    public ContentAdapter getAdapter() {
        return mAdapter;
    }

    public void resumeView() {
        Log.d(TAG, "resumeView()");
        ArrayList<DigestCellView> digestCellViews = mAdapter.getAllDigestCellViews();
        for (DigestCellView dcv : digestCellViews) {
            dcv.resumeView();
        }
    }

    public void pauseView() {
        Log.d(TAG, "pauseView()");
        ArrayList<DigestCellView> digestCellViews = mAdapter.getAllDigestCellViews();
        for (DigestCellView dcv : digestCellViews) {
            dcv.pauseView();
        }
    }

    public static class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.DigestCellViewHolder> {

        private ArrayList<DigestCellView> mAllDigestCellViews = new ArrayList<>();

        public ArrayList<DigestCellView> getAllDigestCellViews() {
            return mAllDigestCellViews;
        }

        public static class DigestCellViewHolder extends RecyclerView.ViewHolder {
            DigestCellView digestCellView;

            public DigestCellViewHolder(LayoutInflater inflater, final ViewGroup parent) {
                super(inflater.inflate(R.layout.digest_view_cell, parent, false));
                digestCellView = (DigestCellView) itemView.findViewById(R.id.digest_cell);
            }
        }

        public ContentAdapter() {

        }

        @Override
        public ContentAdapter.DigestCellViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder");
            DigestCellViewHolder viewHolder = new DigestCellViewHolder(LayoutInflater.from(parent.getContext()), parent);
            mAllDigestCellViews.add(viewHolder.digestCellView);
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
            return 60;
        }


        public void onResume() {

        }

    }
}
