package com.seagate.alto.provider.example.tasks;

import android.os.AsyncTask;

import com.seagate.alto.provider.Provider;

/**
 * Async task to list items in a folder
 */
public class ProviderListFolderTask extends AsyncTask<String, Void, Provider.ListFolderResult> {
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
            return mProvider.listFolder(params[0]);
        } catch (Provider.ProviderException e) {
            mException = e;
            e.printStackTrace();
        }
        return null;
    }
}
