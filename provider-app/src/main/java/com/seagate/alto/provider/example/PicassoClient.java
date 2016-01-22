/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.example;

import android.content.Context;

import com.seagate.alto.provider.DbxProvider;
import com.seagate.alto.provider.PicassoRequestHandler;
import com.seagate.alto.provider.Provider;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

/**
 * Singleton instance of Picasso pre-configured
 */
public class PicassoClient {
    private static Picasso sPicasso;
    private static final int MAX_HEAP_SIZE = (int) Runtime.getRuntime().maxMemory();

    public static final int MAX_DISK_CACHE_SIZE = 80 * 1024 * 1024;
    public static final int MAX_MEMORY_CACHE_SIZE = MAX_HEAP_SIZE / 4;

    public static void init(Context context, Provider provider) {
        // Configure picasso to know about special image requests
        DbxProvider dbx = (DbxProvider) provider;
        sPicasso = new Picasso.Builder(context)
                .downloader(new OkHttpDownloader(context, MAX_DISK_CACHE_SIZE))
                .memoryCache(new LruCache(MAX_MEMORY_CACHE_SIZE))
                .addRequestHandler(new PicassoRequestHandler(dbx))
                .build();
    }

    public static Picasso getPicasso() {
        return sPicasso;
    }
}
