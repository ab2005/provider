/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.imagepipeline;

import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.seagate.alto.provider.LyveCloudProvider;
import com.seagate.alto.provider.Provider;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.HttpUrl;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;

/**
 * Created by abarilov on 2/2/16.
 */
public class LyveCloudFilesCall implements OkHttpNetworkFetcher.Call {
    private static final String TAG = LyveCloudFilesCall.class.getName();

    private final OkHttpNetworkFetcher.OkHttpNetworkFetchState fetchState;
    private final NetworkFetcher.Callback callback;
    private final Provider provider;
    private final com.squareup.okhttp.Call httpCall;

    LyveCloudFilesCall(final OkHttpNetworkFetcher.OkHttpNetworkFetchState fetchState,
                       final NetworkFetcher.Callback callback, Provider provider, OkHttpClient mOkHttpClient) {
        this.fetchState = fetchState;
        this.callback = callback;
        this.provider = provider;

        Uri uri = fetchState.getUri();
        String path = uri.getPath();
        path = path.substring(1, path.length());
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
                .url(LyveCloudProvider.API_BASE_URL + cmd)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + provider.getAccessToken())
                .header("Accept", "*/*")
                .method("POST", RequestBody.create(MediaType.parse(""), args))
                .build();

        httpCall = mOkHttpClient.newCall(request);
    }

    @Override
    public void run() {
        httpCall.enqueue(
                new com.squareup.okhttp.Callback() {
                    @Override
                    public void onResponse(Response response) {
                        fetchState.responseTime = SystemClock.elapsedRealtime();
                        final ResponseBody body = response.body();
                        try {
                            long contentLength = body.contentLength();

                            HttpUrl uri = response.request().httpUrl();
                            Log.d(TAG, uri + " - content length = " + contentLength);
                            Log.d(TAG, uri + " - time(ms) = " + (fetchState.responseTime - fetchState.submitTime));

                            if (contentLength < 0) {
                                contentLength = -1;
                            }
                            callback.onResponse(body.byteStream(), (int) contentLength);
                        } catch (Exception e) {
                            handleException(httpCall, e, callback);
                        } finally {
                            try {
                                body.close();
                            } catch (Exception e) {
                                Log.w(TAG, "Exception when closing response body", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(final Request request, final IOException e) {
                        handleException(httpCall, e, callback);
                    }
                });

    }

    @Override
    public void cancel() {
        httpCall.cancel();
//        handleException(httpCall, null, callback);
    }

    /*
     * Handles exceptions from {@link com.squareup.okhttp.Call}.
     * <p/>
     * <p> OkHttp notifies callers of cancellations via an IOException. If IOException is caught
     * after request cancellation, then the exception is interpreted as successful cancellation
     * and onCancellation is called. Otherwise onFailure is called.
     */
    private void handleException(final com.squareup.okhttp.Call call, final Exception e, final NetworkFetcher.Callback callback) {
        Log.e(TAG, "handleException", e);
        if (call.isCanceled()) {
            callback.onCancellation();
        } else {
            callback.onFailure(e);
        }
    }
}
