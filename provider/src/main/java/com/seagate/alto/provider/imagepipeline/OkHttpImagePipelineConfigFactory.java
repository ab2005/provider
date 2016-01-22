
package com.seagate.alto.provider.imagepipeline;

import android.content.Context;

import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.seagate.alto.provider.Provider;
import com.squareup.okhttp.OkHttpClient;

/**
 * Drawees for getting an {@link ImagePipelineConfig} that uses
 */
public class OkHttpImagePipelineConfigFactory {

    public static ImagePipelineConfig.Builder newBuilder(Context context, OkHttpClient okHttpClient, Provider provider) {
        return ImagePipelineConfig.newBuilder(context).setNetworkFetcher(new OkHttpNetworkFetcher(okHttpClient, provider));
    }
}
