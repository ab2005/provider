// Copyright (c) 2015-16. Seagate Technology PLC. All rights reserved.

package com.seagate.alto;

// the Application class is the first Alto code to execute

import android.app.Application;
import android.util.Log;

import com.seagate.alto.metrics.AltoMetricsEvent;
import com.seagate.alto.metrics.Metrics;
import com.seagate.alto.metrics.MixpanelReporter;
import com.seagate.alto.metrics.SeagateReporter;
import com.seagate.alto.utils.LogUtils;
import com.seagate.alto.utils.ScreenUtils;

public class AltoApplication extends Application {

    private static AltoApplication sMe;
    private static final String TAG = LogUtils.makeTag(AltoApplication.class);

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "onCreate");
        sMe = this;

        startMetrics();

//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//             // I use comic-relief for testing
////             .setDefaultFontPath("fonts/ComicRelief.ttf")
//            .setDefaultFontPath("fonts/helveticaneueltw1g-roman.ttf")
//            .setFontAttrId(R.attr.fontPath)
//            .build()
//        );

        ScreenUtils.init(this);
    }

    public static AltoApplication getInstance() {
        return sMe;
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
