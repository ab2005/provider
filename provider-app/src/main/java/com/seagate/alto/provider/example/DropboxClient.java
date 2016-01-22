package com.seagate.alto.provider.example;

import com.dropbox.core.v2.DbxClientV2;
import com.seagate.alto.provider.DbxProvider;
import com.seagate.alto.provider.Provider;

/**
 * Singleton instance of {@link DbxClientV2} and friends
 */
public class DropboxClient {
    public static final String USER_AGENT = "dropbox-v2-test";
    private static Provider sProvider;

    public static void init(String accessToken) {
        if (sProvider == null) {
            sProvider = new DbxProvider(accessToken, USER_AGENT);
        }
    }

    public static Provider Provider() {
        return sProvider;
    }
}
