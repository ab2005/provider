/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.example;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.dropbox.core.android.Auth;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.Providers;


/**
 * Base class for Activities that require auth tokens.
 * Will redirect to auth flow if needed.
 */
public abstract class AuthActivity extends AppCompatActivity {
    public static final String USER_AGENT = "Alto-Android/0.0.1";
    private Provider mProvider;

    protected Provider getProvider() {
        return mProvider;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        String accessToken = getAccessToken();
        if (accessToken == null) {
            accessToken = Auth.getOAuth2Token();
            if (accessToken == null) return;
            saveAccessToken(accessToken);
        }

        initAndLoadData(accessToken);
    }

    protected String getAccessToken() {
        SharedPreferences prefs = getSharedPreferences("alto-cloud", MODE_PRIVATE);
        return prefs.getString("access-token", null);
    }

    protected void saveAccessToken(String accessToken) {
        SharedPreferences prefs = getSharedPreferences("alto-cloud", MODE_PRIVATE);
        prefs.edit().putString("access-token", accessToken).apply();
    }

    private void initAndLoadData(String accessToken) {
        if (mProvider == null) {
            mProvider = Providers.LOCAL.provider;
//            mProvider = Providers.DROPBOX.provider;
            Providers.DROPBOX.provider.setAccessToken(accessToken);
            loadData();
        }
    }

    protected abstract void loadData();

}
