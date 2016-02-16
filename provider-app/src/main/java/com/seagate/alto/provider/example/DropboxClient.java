/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.example;

import com.dropbox.core.v2.DbxClientV2;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.Providers;

/**
 * Singleton instance of {@link DbxClientV2} and friends
 */
public class DropboxClient {
    public static final String USER_AGENT = "dropbox-v2-test";
    private static Provider sProvider;

    public static Provider Provider() {
        return Providers.DROPBOX.provider;
    }
}
