/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.imagepipeline;

import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.lyve.ServiceGenerator;

import java.io.IOException;
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

    private final OkHttpNetworkFetcher.HttpNetworkFetchState fetchState;
    private final NetworkFetcher.Callback callback;
    private final Provider provider;
    private final okhttp3.Call httpCall;
    private final String path;
    private final ExecutorService mExecutor;

    LyveCloudFilesCall(final OkHttpNetworkFetcher.HttpNetworkFetchState fetchState,
                       final NetworkFetcher.Callback callback,
                       Provider provider,
                       final OkHttpClient mOkHttpClient) {
        this.fetchState = fetchState;
        this.callback = callback;
        this.provider = provider;
        mExecutor = mOkHttpClient.dispatcher().executorService();
        Uri uri = fetchState.getUri();
        path = uri.getPath().substring(1);
        String size = uri.getQueryParameter("size");
        String format = uri.getQueryParameter("format");
        String cmd = "";
        String args = "";
        // FIXME: thumbnail API does not work!!
//        if (size != null) {
        if (false) {
            cmd = "/v1/files/get_thumbnail";
            if (format != null) {
                args = String.format("{\n" +
                        "  \"path\": \"%s\",\n" +
                        "  \"format\": \"%s\",\n" +
                        "  \"size\": \"%s\"\n" +
                        "}", path, format, size);
            } else {
                args = String.format("{\n" +
                        "  \"path\": \"%s\",\n" +
                        "  \"size\": \"%s\"\n" +
                        "}", path, size);
            }
        } else {
            cmd = "/v1/files/download";
            args = String.format("{\"path\":\"%s\"}", path);
        }


        Request request = new Request.Builder()
                .cacheControl(new CacheControl.Builder().noStore().build())
                .url(ServiceGenerator.API_BASE_URL + cmd)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + provider.getAccessToken())
                .header("Accept", "*/*")
                .method("POST", RequestBody.create(MediaType.parse(""), args))
                .build();

        httpCall = mOkHttpClient.newCall(request);
    }

    @Override
    public void run() {
        try {
            Response response = httpCall.execute();
            fetchState.responseTime = SystemClock.elapsedRealtime();
            Log.d(TAG, path + ": got response in " + (fetchState.responseTime - fetchState.submitTime) + " ms" + path);
            final ResponseBody body = response.body();
            try {
                long contentLength = body.contentLength();
                callback.onResponse(body.byteStream(), (int) contentLength);
                long t = SystemClock.elapsedRealtime() - fetchState.submitTime;
                Log.d(TAG, path + ": response consumed in "+ t + " ms");
            } finally {
                try {
                    body.close();
                } catch (Exception e) {
                    Log.e(TAG, path + ": Exception when closing response body", e);
                }
            }
        } catch (IOException e) {
            long t = SystemClock.elapsedRealtime() - fetchState.submitTime;
            Log.d(TAG, path + ": response failed in " + t + " ms");
            callback.onFailure(e);
        }
    }

    @Override
    public void cancel() {
        long t = SystemClock.elapsedRealtime() - fetchState.submitTime;
        Log.d(TAG, path + ": cancel response in " + t + " ms");
        httpCall.cancel();
        callback.onCancellation();
    }
}
