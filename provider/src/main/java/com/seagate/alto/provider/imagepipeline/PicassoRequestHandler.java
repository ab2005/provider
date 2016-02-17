/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.imagepipeline;

import android.net.Uri;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxFiles;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.dropbox.DbxProvider;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;
import com.squareup.picasso.RequestHandler;

import java.io.IOException;

/**
 * A Picasso request handler that gets the thumbnail url for a path
 * Only handles urls like http://seagate/[path_to_file]
 */
public class PicassoRequestHandler extends RequestHandler {
    private static final String SCHEME =  "http";
    private static final String HOST = "seagate";
    private final DbxFiles mFilesClient;
    private final Provider mProvider;

    // TODO: use provider
    public PicassoRequestHandler(DbxProvider provider) {
        DbxFiles filesClient = provider.getFilesClient();
        mFilesClient = filesClient;
        mProvider = null;
    }

    public PicassoRequestHandler(Provider provider) {
        mProvider = provider;
        mFilesClient = null;
    }

    /**
     * Builds a {@link Uri} for a Dropbox file thumbnail suitable for handling by this handler
     */
    public static Uri buildPicassoUri(DbxFiles.FileMetadata file) {
        // TODO: replace with Provider
        // Uri uri = mProvider.getThumbnailUri();
        // Uri uri = mProvider.getThumbnailUri();
        return new Uri.Builder()
                //Uri uri = mProvider.getThumbnailUri();
                .scheme(SCHEME)
                .authority(HOST)
                .path(file.pathLower)
//                .appendQueryParameter("size", size)
//                .appendQueryParameter("path")
                .build();
    }

    @Override
    public boolean canHandleRequest(Request data) {
        // TODO: add provider check
        // mProvider.canHandleRequest(data.uri)
        boolean b = SCHEME.equals(data.uri.getScheme()) && HOST.equals(data.uri.getHost());
        return b;
    }

    @Override
    public Result load(Request request, int networkPolicy) throws IOException {
        try {
            // TODO: separate a thumbnail download from an image download
            DbxFiles.ThumbnailSize sizeLarge = DbxFiles.ThumbnailSize.w1024h768;
            DbxFiles.ThumbnailSize size = DbxFiles.ThumbnailSize.w640h480;
            String path = request.uri.getPath();
            DbxDownloader<DbxFiles.FileMetadata> downloader =
                    mFilesClient.getThumbnailBuilder(path).
                            format(DbxFiles.ThumbnailFormat.jpeg)
                            .size(size)
                            .start();
            Result r = new Result(downloader.body, Picasso.LoadedFrom.NETWORK);
            return r;
        } catch (DbxFiles.GetThumbnailException e) {
            throw new IOException(e);
        } catch (DbxException e) {
            throw new IOException(e);
        }
    }

    public static Uri buildPicassoUri(Provider.FileMetadata file) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(HOST)
                .path(file.pathLower()).build();
    }

    public static Uri buildPicassoThumbnailUri(Provider.FileMetadata file, String format, String size) {
        return new Uri.Builder()
                .scheme(SCHEME)
                .authority(HOST)
                .path(file.pathLower())
                .appendQueryParameter("format", format)
                .appendQueryParameter("size", size)
                .build();
    }

}
