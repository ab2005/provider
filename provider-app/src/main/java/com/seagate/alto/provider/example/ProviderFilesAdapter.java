/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.example;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.seagate.alto.provider.Provider;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter for file list
 */
public class ProviderFilesAdapter extends RecyclerView.Adapter<ProviderFilesAdapter.MetadataViewHolder> {
    private final Provider mProvider;
    private List<Provider.Metadata> mFiles;
    private final Picasso mPicasso;
    private final Callback mCallback;

    public void setFiles(List<Provider.Metadata> files) {
        mFiles = files;
        notifyDataSetChanged();
    }

    public interface Callback {
        void onFolderClicked(Provider.FolderMetadata folder);
        void onFileClicked(Provider.FileMetadata file);
    }

    public ProviderFilesAdapter(Picasso picasso, Callback callback) {
        mPicasso = picasso;
        mCallback = callback;
        mProvider = DropboxClient.Provider();
    }

    @Override
    public MetadataViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        Context context = viewGroup.getContext();
        Provider.Metadata md = mFiles.get(i);
        int layoutId = R.layout.fresco_files_item;
//        if (md.pathLower().contains("camera uploads")) {
              layoutId = R.layout.fresco_item_tile;
//        }
        View view = LayoutInflater.from(context).inflate(layoutId, viewGroup, false);
        return new MetadataViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MetadataViewHolder metadataViewHolder, int i) {
        metadataViewHolder.bind(mFiles.get(i));
    }

    @Override
    public long getItemId(int position) {
        return mFiles.get(position).pathLower().hashCode();
    }

    @Override
    public int getItemCount() {
        return mFiles == null ? 0 : mFiles.size();
    }

    public class MetadataViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView mTextView;
        private final ImageView mImageView;
        private Provider.Metadata mItem;

        public MetadataViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView)itemView.findViewById(R.id.image);
            mImageView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            mTextView = (TextView)itemView.findViewById(R.id.text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mItem instanceof Provider.FolderMetadata) {
                mCallback.onFolderClicked((Provider.FolderMetadata) mItem);
            }  else if (mItem instanceof Provider.FileMetadata) {
                mCallback.onFileClicked((Provider.FileMetadata)mItem);
            }
        }


        public void bind(Provider.Metadata item) {
            mItem = item;
            mTextView.setText(mItem.name());

            if (item instanceof Provider.FileMetadata) {
                MimeTypeMap mime = MimeTypeMap.getSingleton();
                String ext = item.name().substring(item.name().lastIndexOf(".") + 1);
                String type = mime.getMimeTypeFromExtension(ext);
                // Load based on file path
                // Prepending a magic scheme to get it to
                // be picked up by provider's RequestHandler
                Uri uri = ((Provider.FileMetadata) item).thumbnailUri("jpeg", "w1024h780");
                if (type != null && type.startsWith("image/")) {
                    loadImage(uri);
                } else {
                    loadImage(R.drawable.ic_insert_drive_file_blue_36dp);
                }
            } else if (item instanceof Provider.FolderMetadata) {
                loadImage(R.drawable.ic_folder_blue_36dp);
            }
        }

        private void loadImage(Uri uri) {
            if (mImageView instanceof DraweeView) {
                DraweeView drawee = (DraweeView)mImageView;
                ImageRequestBuilder imageRequestBuilder = ImageRequestBuilder.newBuilderWithSource(uri);
                if (UriUtil.isNetworkUri(uri)) {
                    imageRequestBuilder.setProgressiveRenderingEnabled(true);
                } else {
                    imageRequestBuilder.setResizeOptions(new ResizeOptions(
                            mImageView.getLayoutParams().width + 2,
                            mImageView.getLayoutParams().height));
                }
                DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(imageRequestBuilder.build())
                        .setOldController(drawee.getController())
                        .setAutoPlayAnimations(true)
                        .build();
                drawee.setController(draweeController);
            } else {
                mPicasso.load(uri)
                        .placeholder(R.drawable.ic_photo_grey_600_36dp)
                        .error(R.drawable.ic_photo_grey_600_36dp)
                        .into(mImageView);
            }
        }

        private void loadImage(int resId) {
            if (mImageView instanceof SimpleDraweeView) {
                SimpleDraweeView drawee = (SimpleDraweeView)mImageView;
                Uri uri = ImageRequestBuilder.newBuilderWithResourceId(resId).build().getSourceUri();
                drawee.setImageURI(uri);
            } else {
                mPicasso.load(resId).noFade().into(mImageView);
            }

        }
    }
}
