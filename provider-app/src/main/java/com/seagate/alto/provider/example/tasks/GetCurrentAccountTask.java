/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.example.tasks;

import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxUsers;
import com.seagate.alto.provider.DbxProvider;
import com.seagate.alto.provider.Provider;

/**
 * Async task for getting user account info
 */
public class GetCurrentAccountTask extends AsyncTask<Void, Void, DbxUsers.FullAccount> {

    private final DbxUsers mDbxUsersClient;
    private final Callback mCallback;
    private Exception mException;

    public interface Callback {
        void onComplete(DbxUsers.FullAccount result);
        void onError(Exception e);
    }

    public GetCurrentAccountTask(Provider provider, Callback callback) {
        mDbxUsersClient = ((DbxProvider)provider).getUsersClient();;
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(DbxUsers.FullAccount account) {
        super.onPostExecute(account);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onComplete(account);
        }
    }

    @Override
    protected DbxUsers.FullAccount doInBackground(Void... params) {
        try {
            return mDbxUsersClient.getCurrentAccount();
        } catch (DbxException e) {
            mException = e;
            e.printStackTrace();
        }
        return null;
    }
}
