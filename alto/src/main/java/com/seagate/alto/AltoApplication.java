// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

// the Application class is the first Alto code to execute

import android.app.Application;
import android.util.Log;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.seagate.alto.metrics.AltoMetricsEvent;
import com.seagate.alto.metrics.Metrics;
import com.seagate.alto.metrics.MixpanelReporter;
import com.seagate.alto.metrics.SeagateReporter;
import com.seagate.alto.utils.LogUtils;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class AltoApplication extends Application {

    private static final String TAG = LogUtils.makeTag(AltoApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");

        startMetrics();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
             // I use comic-relief for testing
//             .setDefaultFontPath("fonts/ComicRelief.ttf")
            .setDefaultFontPath("fonts/helveticaneueltw1g-roman.ttf")
            .setFontAttrId(R.attr.fontPath)
            .build()
        );

        startFresco();
    }

    private void startMetrics() {
        SeagateReporter seagateReporter = new SeagateReporter();
        Metrics.getInstance().addReporter(seagateReporter);

        MixpanelReporter mixPanelReporter = new MixpanelReporter(getApplicationContext());
        Metrics.getInstance().addReporter(mixPanelReporter);

        Metrics.getInstance().report(AltoMetricsEvent.Startup);
    }

    private void startFresco() {
//        DiskCacheConfig diskCacheConfig = DiskCacheConfig.newBuilder()
//                .setBaseDirectoryName("SeagateCloud/ImageCache")
//                .setBaseDirectoryPath(getExternalFilesDir(Environment.DIRECTORY_PICTURES))
//                .setMaxCacheSize(50000000)
//                .build();
//        DiskCacheConfig smallImageDiskCacheConfig = DiskCacheConfig.newBuilder()
//                .setBaseDirectoryName("SeagateCloud/SmallImageCache")
//                .setBaseDirectoryPath(getExternalFilesDir(Environment.DIRECTORY_PICTURES))
//                .setMaxCacheSize(10000000)
//                .build();
        ImagePipelineConfig config = ImagePipelineConfig.newBuilder(this)
//                .setMainDiskCacheConfig(diskCacheConfig)
//                .setSmallImageDiskCacheConfig(smallImageDiskCacheConfig)
                .build();
        Fresco.initialize(this, config);
    }


}
