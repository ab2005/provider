// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.metrics;

public interface IMetricsReporter {

    void reportEvent(IMetricsEvent metricsEvent, long start, long duration);
    void flush();  // make sure to send the data

}
