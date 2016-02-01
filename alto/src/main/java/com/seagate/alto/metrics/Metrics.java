// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.metrics;

// class for organizing metrics reporters

import android.util.Log;

import com.seagate.alto.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

// each analytics service is separated into its own reporter

// usage:
// start to start a timer for an event,
// report ends a started event or just reports an instance of an event

public class Metrics {

    private static final String TAG = LogUtils.makeTag(Metrics.class);

    private static Metrics mInstance;

    public static Metrics getInstance() {
        if (mInstance == null) {
            mInstance = new Metrics();
        }
        return mInstance;
    }

    private List<IMetricsReporter> mReporters = new ArrayList<>();

    // manage reporters

    public void addReporter(IMetricsReporter reporter) {
        mReporters.add(reporter);
    }
    public void removeReporter(IMetricsReporter reporter) {
        mReporters.add(reporter);
    }

    // public calls for reporting

    public void report(IMetricsEvent event) {
        report(event, System.currentTimeMillis());
    }

    public void report(IMetricsEvent event, long when) {

        for (IMetricsReporter reporter : mReporters) {
            try {
                reporter.reportEvent(event, when);
            } catch (Exception e) {
                Log.e(TAG, "exception caught when reporting " + reporter);
                e.printStackTrace();
            }
        }
    }

    // flush the data to the servers
    public void flush() {
        for (IMetricsReporter reporter : mReporters) {
            try {
                reporter.flush();
            } catch (Exception e) {
                Log.e(TAG, "exception caught when flushing " + reporter);
                e.printStackTrace();
            }
        }
    }

}
