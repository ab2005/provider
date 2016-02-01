/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider;

import android.os.AsyncTask;

/**
 * Async task to list items in a folder
 */
public class ListFolderTask extends AsyncTask<String, Void, Provider.ListFolderResult> {
    private final Provider mProvider;
    private Exception mException;
    private Callback mCallback;

    public interface Callback {
        void onDataLoaded(Provider.ListFolderResult result);
        void onError(Exception e);
    }

    public ListFolderTask(Provider provider, Callback callback) {
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
            return mProvider.listFolder(params[0]);
        } catch (Provider.ProviderException e) {
            mException = e;
            e.printStackTrace();
        }
        return null;
    }
}
