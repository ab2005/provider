// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.metrics;

// this class reports data to the seagate servers

public class SeagateReporter implements IMetricsReporter{

    public SeagateReporter() {

    }

    @Override
    public void reportEvent(IMetricsEvent metricsEvent, long start, long duration) {

        // send the info to the seagate service

    }

    @Override
    public void flush() {
        // make sure the data goes out
    }
}
