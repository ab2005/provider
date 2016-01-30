// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

// the Application class is the first Alto code to execute

import android.app.Application;
import android.util.Log;

import com.facebook.common.logging.FLog;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.listener.RequestListener;
import com.facebook.imagepipeline.listener.RequestLoggingListener;
import com.seagate.alto.utils.LogUtils;
import com.seagate.alto.utils.ScreenUtils;

import java.util.HashSet;
import java.util.Set;

public class AltoApplication extends Application {

    private static AltoApplication sMe;
    private static final String TAG = LogUtils.makeTag(AltoApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");
        sMe = this;

//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//             // I use comic-relief for testing
////             .setDefaultFontPath("fonts/ComicRelief.ttf")
//            .setDefaultFontPath("fonts/helveticaneueltw1g-roman.ttf")
//            .setFontAttrId(R.attr.fontPath)
//            .build()
//        );

        ScreenUtils.init(this);
        startFresco();
    }

    public static AltoApplication getInstance() {
        return sMe;
    }

    private void startFresco() {
        Set<RequestListener> requestListeners = new HashSet<>();
        requestListeners.add(new RequestLoggingListener());

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
                .setRequestListeners(requestListeners)
//                .setMainDiskCacheConfig(diskCacheConfig)
//                .setSmallImageDiskCacheConfig(smallImageDiskCacheConfig)
                .build();
        Fresco.initialize(this, config);
        FLog.setMinimumLoggingLevel(FLog.VERBOSE);
    }


}
