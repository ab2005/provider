/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.imagepipeline;

import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxFiles;
import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.seagate.alto.provider.DbxProvider;

import java.io.IOException;

class DbxFilesCall implements OkHttpNetworkFetcher.Call {
    private static final String TAG = "DbxFilesCall";
    private final OkHttpNetworkFetcher.HttpNetworkFetchState fetchState;
    private final NetworkFetcher.Callback callback;
    private final DbxProvider provider;
    private DbxDownloader<DbxFiles.FileMetadata> downloader = null;

    DbxFilesCall(final OkHttpNetworkFetcher.HttpNetworkFetchState fetchState, final NetworkFetcher.Callback callback, DbxProvider provider) {
        this.fetchState = fetchState;
        this.callback = callback;
        this.provider = provider;
    }

    @Override
    public void run() {
        try {
            downloader = downloader();
            fetchState.responseTime = SystemClock.elapsedRealtime();
            callback.onResponse(downloader.body, -1);
        } catch (DbxException | IOException e) {
            Log.d(TAG, " FAILED REQUEST: " + fetchState.getUri());
            if (downloader != null) {
                try {
                    downloader.close();
                } catch (IllegalStateException ex) {
                    // ignore
                }
                downloader = null;
                callback.onFailure(e);
            }
        }
    }

    @Override
    public void cancel() {
        if (downloader != null) {
            try {
                downloader.close();
            } catch (IllegalStateException | NullPointerException e) {
                // ignore
                Log.e(TAG, e.getMessage());
            }
            downloader = null;
            callback.onCancellation();
        }
    }

    private DbxDownloader<DbxFiles.FileMetadata> downloader() throws DbxException {
        Uri uri = fetchState.getUri();
        if (isFullSize()) {
            return provider.getFilesClient()
                    .downloadBuilder(uri.getPath())
                    .start();
        } else {
            return provider.getFilesClient()
                    .getThumbnailBuilder(uri.getPath())
                    .format(thumbnailFormat())
                    .size(thumbnailSize())
                    .start();
        }
    }

    private boolean isFullSize() {
        Uri uri = fetchState.getUri();
        return (uri.getQueryParameter("format") == null) && (uri.getQueryParameter("size") == null);
    }

    private DbxFiles.ThumbnailFormat thumbnailFormat() {
        String format = fetchState.getUri().getQueryParameter("format");
        if (format == null) {
            format = "jpeg";
        }
        switch (format) {
            case "png":
                return DbxFiles.ThumbnailFormat.png;
            default:
            case "jpeg":
                return DbxFiles.ThumbnailFormat.jpeg;
        }
    }

    private DbxFiles.ThumbnailSize thumbnailSize() {
        String size = fetchState.getUri().getQueryParameter("size");
        if (size == null) {
            size = "w128h128";
        }
        switch (size) {
            case "w32h32":
                return DbxFiles.ThumbnailSize.w32h32;
            case "w64h64":
                return DbxFiles.ThumbnailSize.w64h64;
            default:
            case "w128h128":
                return DbxFiles.ThumbnailSize.w128h128;
            case "w640h480":
                return DbxFiles.ThumbnailSize.w640h480;
            case "w1024h768":
                return DbxFiles.ThumbnailSize.w1024h768;
        }
    }
}
