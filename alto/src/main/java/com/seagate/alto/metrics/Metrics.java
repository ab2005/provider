// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.metrics;

// class for organizing metrics reporters

import android.util.Log;

import com.seagate.alto.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, EventTracker> mDurationMap = new HashMap<>();

    private class EventTracker {
        long start;

        // for debugging, we could store a stack trace

        public EventTracker(long start) {
            this.start = start;
        }
    }

    // manage reporters

    public void addReporter(IMetricsReporter reporter) {
        mReporters.add(reporter);
    }

    public void removeReporter(IMetricsReporter reporter) {
        mReporters.add(reporter);
    }

    // when an event is started, create a duration object
    // there can only be one duration object for each event

    public void start(IMetricsEvent event) {

        EventTracker duplicate = mDurationMap.get(event.getEventName());
        if (duplicate != null) {
            Log.e(TAG, "event already started");
        }

        EventTracker d = new EventTracker(System.currentTimeMillis());
        mDurationMap.put(event.getEventName(), d);
    }

    public void report(IMetricsEvent event) {

        long now = System.currentTimeMillis();

        long start = 0;
        long duration = 0;

        EventTracker d = mDurationMap.get(event.getEventName());

        if (d == null) {
            start = now;
        } else {
            start = d.start;
            duration = now - start;

            mDurationMap.remove(d);
        }

        for (IMetricsReporter reporter : mReporters) {
            reporter.reportEvent(event, start, duration);
        }
    }

    // flush the data to the servers
    public void flush() {
        for (IMetricsReporter reporter : mReporters) {
            reporter.flush();
        }
    }

}
