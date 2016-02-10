// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.metrics;

// events for Lyve Cloud Clients

public enum AltoMetricsEvent implements IMetricsEvent {


    ClientLaunched(0),
    CreateAccount(1),
    Login(2),

    ;  // end of constants

    private final int mId;

    private AltoMetricsEvent(final int id) {
        mId = id;
    }

    @Override
    public String getEventName() {
        return this.name();
    }

    @Override
    public int getEventValue() {
        return mId;
    }
}
