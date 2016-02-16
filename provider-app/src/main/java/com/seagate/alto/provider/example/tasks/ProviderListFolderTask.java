/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.example.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.seagate.alto.provider.LyveCloudProvider;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.Providers;

import java.io.IOException;

/**
 * Async task to list items in a folder
 */
public class ProviderListFolderTask extends AsyncTask<String, Void, Provider.ListFolderResult> {
    private static final String TAG = ProviderListFolderTask.class.getName();
    private final Provider mProvider;
    private Exception mException;
    private Callback mCallback;

    public interface Callback {
        void onDataLoaded(Provider.ListFolderResult result);
        void onError(Exception e);
    }

    public ProviderListFolderTask(Provider provider, Callback callback) {
        mProvider = provider;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Provider.ListFolderResult result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(result);
        }
    }

    @Override
    protected Provider.ListFolderResult doInBackground(String... params) {
        try {
            if (mProvider == Providers.SEAGATE.provider) {
                if (mProvider.getAccessToken() == null) {
                    String token = null;
                    try {
                        token = LyveCloudProvider.login("demo.zzz@seagate.com", "demozzz");
                        mProvider.setAccessToken(token);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return mProvider.listFolder("/d6f14c1e-ce88-4ebf-aa2f-f50fc7250dc4/Demo1/test");
            } else {
                return mProvider.listFolder(params[0]);
            }
        } catch (Provider.ProviderException e) {
            mException = e;
            Log.e(TAG, "Failed to list folder");
        }
        return null;
    }
}
