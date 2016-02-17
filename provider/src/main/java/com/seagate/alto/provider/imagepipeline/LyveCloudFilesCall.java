/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.imagepipeline;

import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.seagate.alto.provider.dropbox.DbxProvider;
import com.seagate.alto.provider.lyve.LyveCloudProvider;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.network.ServiceGenerator;

import java.util.concurrent.ExecutorService;

import okhttp3.CacheControl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class LyveCloudFilesCall implements OkHttpNetworkFetcher.Call {
    private static final String TAG = LyveCloudFilesCall.class.getSimpleName();

    private final OkHttpNetworkFetcher.HttpNetworkFetchState mFetchState;
    private final NetworkFetcher.Callback mCallback;
    private final Provider mProvider;
    private final okhttp3.Call mHttpCall;
    private final String path;
    private final ExecutorService mExecutor;

    LyveCloudFilesCall(final OkHttpNetworkFetcher.HttpNetworkFetchState fetchState,
                       final NetworkFetcher.Callback callback,
                       Provider provider,
                       final OkHttpClient mOkHttpClient) {
        this.mFetchState = fetchState;
        this.mCallback = callback;
        this.mProvider = provider;
        mExecutor = mOkHttpClient.dispatcher().executorService();
        Uri uri = fetchState.getUri();
        path = uri.getPath().substring(1);
        Request request = null;
        if (provider instanceof LyveCloudProvider) {
            request = createLyveCloudRequest(uri);
        } else if (provider instanceof DbxProvider) {
            request = createDropboxRequest(uri);
        }
        Log.d(TAG, path + ": creating call...");
        mHttpCall = mOkHttpClient.newCall(request);
        Log.d(TAG, path + ": created call");
    }

    @Override
    public void run() {
        try {
            Log.d(TAG, path + ": running call..");
            Response response = mHttpCall.execute();
            if (!response.isSuccessful()) {
                Log.d(TAG, path + ": response failed! " + response.message());
                return;
            }
            mFetchState.responseTime = SystemClock.elapsedRealtime();
            Log.d(TAG, path + ": got response in " + (mFetchState.responseTime - mFetchState.submitTime) + " ms");
            final ResponseBody body = response.body();
            try {
                long contentLength = body.contentLength();
                mCallback.onResponse(body.byteStream(), (int) contentLength);
                long t = SystemClock.elapsedRealtime() - mFetchState.submitTime;
                Log.d(TAG, path + ": response consumed in "+ t + " ms");
            } finally {
                try {
                    body.close();
                } catch (Exception e) {
                    Log.e(TAG, path + ": Exception when closing response body", e);
                }
            }
        } catch (Exception e) {
            long t = SystemClock.elapsedRealtime() - mFetchState.submitTime;
            Log.d(TAG, path + ": response failed in " + t + " ms,  " + e.getMessage());
            mCallback.onFailure(e);
            e.printStackTrace();
        }
    }

    @Override
    public void cancel() {
        long t = SystemClock.elapsedRealtime() - mFetchState.submitTime;
        Log.d(TAG, path + ": cancel response in " + t + " ms");
        mHttpCall.cancel();
        mCallback.onCancellation();
    }

    private Request createLyveCloudRequest(Uri uri) {
        String size = uri.getQueryParameter("size");
        String format = uri.getQueryParameter("format");
        String cmd = "";
        String args = "";
        // TODO: thumbnail API does not work!!
//        if (size != null || foarmat != null) {
        if (false) {
            cmd = "/v1/files/get_thumbnail";
            if (format == null) {
                format = "jpeg";
            }
            if (size == null) {
                size = "w64h64";
            }
            args = String.format("{\n" +
                    "  \"path\": \"%s\",\n" +
                    "  \"format\": \"%s\",\n" +
                    "  \"size\": \"%s\"\n" +
                    "}", path, format, size);
        } else {
            cmd = "/v1/files/download";
            args = String.format("{\"path\":\"%s\"}", path);
        }

        Request request = new Request.Builder()
                .cacheControl(new CacheControl.Builder().noStore().build())
                .url(ServiceGenerator.API_BASE_URL + cmd)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + mProvider.getAccessToken())
                .header("Accept", "*/*")
                .method("POST", RequestBody.create(MediaType.parse(""), args))
                .build();

        return request;
    }

    private Request createDropboxRequest(Uri uri) {
        String size = uri.getQueryParameter("size");
        String format = uri.getQueryParameter("format");
        String cmd = null;
        String args = null;
        String path = Uri.decode(uri.getPath());
        if (size == null && format == null) {
            // full size image
            cmd = "/2/files/download";
            args = String.format("{\"path\":\"%s\"}", path);
        } else {
            cmd = "/2/files/get_thumbnail";
            if (format == null) {
                format = "jpeg";
            }
            if (size == null) {
                size = "w64h64";
            }
            args = String.format("{" +
                    "  \"path\": \"%s\"," +
                    "  \"format\":{\".tag\": \"%s\"}," +
                    "  \"size\":{\".tag\":\"%s\"}" +
                    "}", path, format, size);
        }

        Request request = null;
        Log.d(TAG, path + ": creating request...");
        try {
            new Request.Builder()
                    .cacheControl(new CacheControl.Builder().noStore().build())
                    .url(ServiceGenerator.DBX_API_BASE_URL + cmd)
                    .header("Authorization", "Bearer " + mProvider.getAccessToken())
                    .header("Connection", "Keep-Alive")
                    .header("Dropbox-API-Arg", args)
                    .method("POST", RequestBody.create(MediaType.parse(""), args))
                    .build();
            Log.d(TAG, path + ": created request");
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }

        return request;
    }

}
