// Copyright (c) 2015-2016. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.utils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v4.content.FileProvider;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nullable;

public class SharingUtils {
    private final static String TAG = LogUtils.makeTag(SharingUtils.class);

    public static void shareImageFromRecyclerView(int position, final Activity activity) {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        ImageRequest imageRequest = ImageRequestBuilder
                .newBuilderWithSource(PlaceholderContent.getThumbnailUri(position))
                .setLowestPermittedRequestLevel(ImageRequest.RequestLevel.FULL_FETCH)
                .build();
        final DataSource<CloseableReference<CloseableImage>> dataSource =
                imagePipeline.fetchDecodedImage(imageRequest, activity);

        dataSource.subscribe(new BaseBitmapDataSubscriber() {
            @Override
            public void onNewResultImpl(@Nullable Bitmap bitmap) {
                if (bitmap == null) {
                    Log.d(TAG, "Bitmap data source returned success, but bitmap is null.");
                    return;
                }

                // save bitmap to cache directory
                try {

                    File cachePath = new File(activity.getCacheDir(), "images");
                    cachePath.mkdirs(); // don't forget to make the directory
                    FileOutputStream stream = new FileOutputStream(cachePath + "/image.jpg"); // overwrites this image every time
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    stream.close();

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                File imagePath = new File(activity.getCacheDir(), "images");
                File newFile = new File(imagePath, "image.jpg");
                Uri contentUri = FileProvider.getUriForFile(activity, "com.seagate.alto.fileprovider", newFile);

                if (contentUri != null) {

                    Intent shareIntent = new Intent();
                    shareIntent.setAction(Intent.ACTION_SEND);
                    shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION); // temp permission for receiving app to read this file
                    shareIntent.setType("image/*");
                    shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    activity.startActivity(Intent.createChooser(shareIntent, "Choose an app"));

                }
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
