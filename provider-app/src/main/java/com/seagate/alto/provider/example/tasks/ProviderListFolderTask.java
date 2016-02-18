/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.example.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.seagate.alto.provider.lyve.LyveCloudProvider;
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
                        token = LyveCloudProvider.login("abarilov@geagate.com", "pitkelevo");
                        mProvider.setAccessToken(token);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                Provider.ListFolderResult res = null;
                try {
                    res = mProvider.listFolder("");
                } catch (Provider.ProviderException e) {
                    e.printStackTrace();
                    return null;
                }

                String deviceRoot = res.entries().get(0).pathLower();
                String path = deviceRoot + "/Photos/thumbs";
                return mProvider.listFolder(path);
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
