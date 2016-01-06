package com.seagate.alto.ui;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.facebook.drawee.view.SimpleDraweeView;
import com.seagate.alto.PlaceholderContent;
import com.seagate.alto.R;

public class DigestListView extends RecyclerView {

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

            SimpleDraweeView drawee;
            int position;

            public DigestCellViewHolder(LayoutInflater inflater, final ViewGroup parent) {
                super(inflater.inflate(R.layout.digest_view_cell, parent, false));
                final DigestCellView digestCellView = (DigestCellView) itemView.findViewById(R.id.group);
                if (digestCellView != null) {
                    digestCellView.loadContent();           // TODO: 1/5/16 load it now? or later? (onBindViewHolder)
                }

//                super(inflater.inflate(R.layout.bill_card, parent, false));
//                drawee = (SimpleDraweeView) itemView.findViewById(R.id.card_image);

            }
        }

        public ContentAdapter() {

        }

        @Override
        public ContentAdapter.DigestCellViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            DigestCellViewHolder viewHolder = new DigestCellViewHolder(LayoutInflater.from(parent.getContext()), parent);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(DigestCellViewHolder holder, int position) {

//            Uri uri = PlaceholderContent.getUri(position);
//            holder.drawee.setImageURI(uri);

            holder.position = position;
        }

        @Override
        public int getItemCount() {
            return 10;
        }

    }
}
