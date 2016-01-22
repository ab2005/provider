package com.seagate.alto.provider.example.tasks;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxFiles;
import com.seagate.alto.provider.DbxProvider;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.UriHelpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Async task to upload a file to a directory
 */
public class UploadFileTask extends AsyncTask<String, Void, Provider.FileMetadata> {

    private final Context mContext;
    private final DbxFiles mFilesClient;
    private Exception mException;
    private Callback mCallback;

    public interface Callback {
        void onUploadComplete(Provider.FileMetadata result);
        void onError(Exception e);
    }

    public UploadFileTask(Context context, Provider provider, Callback callback) {
        mContext = context;
        mFilesClient = ((DbxProvider)provider).getFilesClient();
        mCallback = callback;
    }

    @Override
    protected void onPostExecute(Provider.FileMetadata result) {
        super.onPostExecute(result);
        if (mException != null) {
            mCallback.onError(mException);
        } else if (result == null) {
            mCallback.onError(null);
        } else {
            mCallback.onUploadComplete(result);
        }
    }

    protected Provider.FileMetadata doInBackground(String... params) {
        String localUri = params[0];
        String localFilePath = UriHelpers.getFilePathForUri(mContext, Uri.parse(localUri));

        if (localFilePath == null) {
            // error
            return null;
        }

        File localFile = new File(localFilePath);
        final String remoteFolderPath = params[1];

        // Note - this is not ensuring the name is a valid file name
        String remoteFileName = localFile.getName();

        try {
            InputStream inputStream = new FileInputStream(localFile);
            try {
                final DbxFiles.FileMetadata result = mFilesClient
                        .uploadBuilder(remoteFolderPath + "/" + remoteFileName)
                        .mode(DbxFiles.WriteMode.overwrite)
                        .run(inputStream);
                return new DbxProvider.FileMetadataImpl(result);
            } finally {
                inputStream.close();
            }

        } catch (DbxException | IOException e) {
            e.printStackTrace();
            mException = e;
        }

        // Error
        return null;
    }
}
