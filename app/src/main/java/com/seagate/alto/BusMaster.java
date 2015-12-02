package com.seagate.alto;

// use a singleton for now to avoid injection

import com.squareup.otto.Bus;

public class BusMaster {

    static Bus mBus;

    static public Bus getBus() {

        if (mBus == null) {
            mBus = new Bus();
        }

        return mBus;
    }

}
