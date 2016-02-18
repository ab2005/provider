/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.example;

// the Application class is the first Alto code to execute

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.seagate.alto.provider.Providers;

public class ProviderApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Providers.initWithDefaults(this);
        Fresco.getImagePipeline().clearCaches();
    }
}
