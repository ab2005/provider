
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.imagepipeline;


import android.content.Context;
import android.util.SparseIntArray;

import com.facebook.cache.disk.DiskCacheConfig;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.listener.RequestLoggingListener;
import com.facebook.imagepipeline.memory.PoolConfig;
import com.facebook.imagepipeline.memory.PoolFactory;
import com.facebook.imagepipeline.memory.PoolParams;
import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.seagate.alto.provider.Provider;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import okhttp3.OkHttpClient;

/**
 * Creates ImagePipeline configuration that uses {@link }OkHttpNetworkFetcher}
 * with OkHttp as a backend for {@link Provider} calls.
 */
public class ConfigFactory {
    private static final String IMAGE_PIPELINE_CACHE_DIR = "imagepipeline_cache";
    public final static int MAX_REQUEST_PER_TIME = 164;

    /**
     * Default config with {@link OkHttpNetworkFetcher} as network backend.
     */
    public static com.facebook.imagepipeline.core.ImagePipelineConfig getDefaultConfig(Context context) {
        Set<RequestListener> requestListeners = new HashSet<>(
                Arrays.asList(new RequestListener[]{new RequestLoggingListener()}));

        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder(context)
                .setBaseDirectoryPath(context.getApplicationContext().getCacheDir())
                .setBaseDirectoryName(IMAGE_PIPELINE_CACHE_DIR)
                .setMaxCacheSize(ConfigConstants.MAX_DISK_CACHE_SIZE)
                .build();

        OkHttpClient httpClient = new OkHttpClient.Builder()
                //.addInterceptor(new StethoInterceptor())
                .build();

        NetworkFetcher networkFetcher = new OkHttpNetworkFetcher(httpClient);

        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(context)
                .setDownsampleEnabled(true)
                .setWebpSupportEnabled(true)
                .setMainDiskCacheConfig(diskCacheConfig)
                .setNetworkFetcher(networkFetcher)
                .setRequestListeners(requestListeners)
                .build();

        return config;
    }

    /**
     * Default builder with custom pool factory (see:{@link PoolFactory}).
     */
    public static ImagePipelineConfig.Builder newBuilder(Context context, OkHttpClient okHttpClient) {
        SparseIntArray defaultBuckets = new SparseIntArray();
        defaultBuckets.put(16 * 1024, MAX_REQUEST_PER_TIME);
        PoolParams smallByteArrayPoolParams = new PoolParams(
                16 * 1024 * MAX_REQUEST_PER_TIME,
                4 * 1024 * 1024,
                defaultBuckets);
        PoolFactory factory = new PoolFactory(PoolConfig.newBuilder()
                .setSmallByteArrayPoolParams(smallByteArrayPoolParams)
                .build());
        return ImagePipelineConfig
                .newBuilder(context)
                .setDownsampleEnabled(true)
                .setPoolFactory(factory)
                .setNetworkFetcher(new OkHttpNetworkFetcher(okHttpClient));
    }
}
