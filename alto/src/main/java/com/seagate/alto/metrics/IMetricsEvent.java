// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.metrics;

public interface IMetricsEvent {
    String getEventName();
    int getEventValue();
}
