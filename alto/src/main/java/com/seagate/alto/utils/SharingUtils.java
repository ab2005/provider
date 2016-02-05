package com.seagate.alto.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.facebook.common.executors.CallerThreadExecutor;
import com.facebook.common.references.CloseableReference;
import com.facebook.datasource.DataSource;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.datasource.BaseBitmapDataSubscriber;
import com.facebook.imagepipeline.image.CloseableImage;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.seagate.alto.PlaceholderContent;

import javax.annotation.Nullable;

public class SharingUtils {
    private final static String TAG = LogUtils.makeTag(SharingUtils.class);

    public static void shareImageFromRecyclerView(int position, final Context context) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(PlaceholderContent.getThumbnailUri(position))
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .build();
        final DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchDecodedImage(imageRequest, context);

        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable Bitmap bitmap) {
                if (bitmap == null) {
                    Log.d(TAG, "Bitmap data source returned success, but bitmap null.");
                    return;
                }

                Log.d(TAG, "sharing bitmap: " + bitmap.getWidth() + "/" + bitmap.getHeight() + ", " + bitmap);

                String path = MediaStore.Images.Media.insertImage(context.getContentResolver(),
                        bitmap, "Image Description", null);
                Uri bmpUri = Uri.parse(path);
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, bmpUri);
                shareIntent.setType("image/*");

                context.startActivity(Intent.createChooser(shareIntent, "Share Image"));

                if (dataSource != null) {
                    dataSource.close();
                }
            }

            @Override
            public void onFailureImpl(DataSource dataSource) {
                // No cleanup required here
                if (dataSource != null) {
                    dataSource.close();
                }
            }
        }, CallerThreadExecutor.getInstance());
    }
}
