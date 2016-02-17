/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.example.tasks;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxFiles;
import com.seagate.alto.provider.dropbox.DbxProvider;
import com.seagate.alto.provider.Provider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Task to download a file from Dropbox and put it in the Downloads folder
 */
public class DownloadFileTask extends AsyncTask<Provider.FileMetadata, Void, File> {

    private static final String TAG = DownloadFileTask.class.getName();
    private final Context mContext;
    private final DbxFiles mFilesClient;
    private Exception mException;
    private Callback mCallback;

    public interface Callback {
        void onDownloadComplete(File result);
        void onError(Exception e);
    }

    public DownloadFileTask(Context context, Provider provider, Callback callback) {
        mContext = context;
        mFilesClient = ((DbxProvider)provider).getFilesClient();
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else {
            mCallback.onDownloadComplete(result);
        }
    }

    @Override
    protected File doInBackground(Provider.FileMetadata... params) {
        Provider.FileMetadata metadata = params[0];
        try {
            final File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(path, metadata.name());

            // Upload the file.
            OutputStream outputStream = new FileOutputStream(file);
            try {
                // TODO: Replace with Provider API
                mFilesClient.downloadBuilder(metadata.pathLower()).rev(metadata.rev()).run(outputStream);
            } finally {
                outputStream.close();
            }

            // Tell android about the file
            MediaScannerConnection.scanFile(
                    mContext,
                    new String[]{file.getAbsolutePath()},
                    null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.v(TAG, "File " + path + " was scanned seccessfully: " + uri);
                        }
                    });
            return file;
        } catch (DbxException | IOException e) {
            e.printStackTrace();
            mException = e;
        }

        return null;
    }
}
