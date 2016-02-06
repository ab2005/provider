/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.utils.provider;

import android.os.AsyncTask;

import com.seagate.alto.provider.Provider;

/**
 * Async task to list items in a folder
 */
public class SearchImagesTask extends AsyncTask<String, Void, Provider.SearchResult> {
    private final Provider mProvider;
    private Exception mException;
    private Callback mCallback;

    public interface Callback {
        void onDataLoaded(Provider.SearchResult result);
        void onError(Exception e);
    }

    public SearchImagesTask(Provider provider, Callback callback) {
        mProvider = provider;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Provider.SearchResult result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDataLoaded(result);
        }
    }

    @Override
    protected Provider.SearchResult doInBackground(String... params) {
        try {
            return mProvider.search("/camera uploads", ".jpg");
        } catch (Provider.ProviderException e) {
            mException = e;
            e.printStackTrace();
        }
        return null;
    }
}
