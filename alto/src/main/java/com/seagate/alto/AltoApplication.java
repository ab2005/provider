// Copyright (c) 2015-16. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

// the Application class is the first Alto code to execute

import android.app.Application;

import com.seagate.alto.metrics.AltoMetricsEvent;
import com.seagate.alto.metrics.Metrics;
import com.seagate.alto.metrics.MixpanelReporter;
import com.seagate.alto.metrics.SeagateReporter;
import com.seagate.alto.provider.Providers;
import com.seagate.alto.utils.LogUtils;
import com.seagate.alto.utils.ScreenUtils;

public class AltoApplication extends Application {

    private static final String TAG = LogUtils.makeTag(AltoApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();
        startMetrics();
        ScreenUtils.init(this);

        Providers.initWithDefaults(this);
    }

    private void startMetrics() {
        SeagateReporter seagateReporter = new SeagateReporter(this);
        Metrics.getInstance().addReporter(seagateReporter);

        MixpanelReporter mixPanelReporter = new MixpanelReporter(this);
        Metrics.getInstance().addReporter(mixPanelReporter);

        // while we're here, let everyone know we are launched
        Metrics.getInstance().report(AltoMetricsEvent.ClientLaunched);
    }

}
