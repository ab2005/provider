// Copyright (c) 2015. Seagate Technology PLC. All rights reserved.

package com.seagate.alto.metrics;

// events for Lyve Cloud Clients

public enum AltoMetricsEvent implements IMetricsEvent {


    CreateNewAccount(1000),
    LogintoExistingAccount(1001),
    RestoreContentfromCloud(1002),
    ReplaceDrive(1003),
    EmptyTrash(1004),

    // test events
    Startup(100),
    ShowSplash(10000),

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
