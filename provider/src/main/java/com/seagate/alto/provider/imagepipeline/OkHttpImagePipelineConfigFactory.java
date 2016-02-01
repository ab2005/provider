
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.imagepipeline;

import android.content.Context;

import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.seagate.alto.provider.Provider;
import com.squareup.okhttp.OkHttpClient;

/**
 * Creates ImagePipeline configuration that uses {@link }OkHttpNetworkFetcher}
 * with OkHttp as a backend for {@link Provider} calls.
 */
public class OkHttpImagePipelineConfigFactory {
//    public final static int MAX_REQUEST_PER_TIME = 164;

    public static ImagePipelineConfig.Builder newBuilder(Context context, OkHttpClient okHttpClient, Provider provider) {
//        SparseIntArray defaultBuckets = new SparseIntArray();
//        defaultBuckets.put(16 * ByteConstants.KB, MAX_REQUEST_PER_TIME);
//        PoolParams smallByteArrayPoolParams = new PoolParams(
//                16 * ByteConstants.KB * MAX_REQUEST_PER_TIME,
//                4 * ByteConstants.MB,
//                defaultBuckets);
//        PoolFactory factory = new PoolFactory(
//                PoolConfig.newBuilder()
//                        .setSmallByteArrayPoolParams(smallByteArrayPoolParams)
//                        .build());
        return ImagePipelineConfig
                .newBuilder(context)
                .setDownsampleEnabled(true)
//                .setPoolFactory(factory)
                .setNetworkFetcher(new OkHttpNetworkFetcher(okHttpClient, provider));
    }
}
