/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider;

import android.net.Uri;
import android.os.Looper;
import android.os.SystemClock;

import com.facebook.common.logging.FLog;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;


/**
 * Fresco network fetcher that uses OkHttp as a backend.
 */
public class FrescoNetworkFetcher implements NetworkFetcher<FrescoNetworkFetcher.OkHttpNetworkFetchState> {

    public static class OkHttpNetworkFetchState extends FetchState {
        public long submitTime;
        public long responseTime;
        public long fetchCompleteTime;

        public OkHttpNetworkFetchState(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
            super(consumer, producerContext);
        }
    }

    private static final String REPO_HEADER_AUTH = "Authorization";
    private static final String REPO_HEADER_ARG =  "Dropbox-API-Arg";
    private static final String REPO_URL = "https://content.dropboxapi.com/2/files/";
    private static final String TAG = "OkHttpNetworkFetchProducer";
    private static final String QUEUE_TIME = "queue_time";
    private static final String FETCH_TIME = "fetch_time";
    private static final String TOTAL_TIME = "total_time";
    private static final String IMAGE_SIZE = "image_size";

    private final OkHttpClient mOkHttpClient;

    private Executor mCancellationExecutor;

    /**
     * @param okHttpClient client to use
     */
    public FrescoNetworkFetcher(OkHttpClient okHttpClient) {
        mOkHttpClient = okHttpClient;
        mCancellationExecutor = okHttpClient.getDispatcher().getExecutorService();
    }

    @Override
    public OkHttpNetworkFetchState createFetchState(Consumer<EncodedImage> consumer, ProducerContext context) {
        return new OkHttpNetworkFetchState(consumer, context);
    }

    static Request buildRequest(Uri uri) {
        String auth = uri.getQueryParameter("auth");
        String path = uri.getPath();
        String size = uri.getQueryParameter("size");
        String format = uri.getQueryParameter("format");
        String rev = uri.getQueryParameter("rev");
        String cmd = "";
        String args = "";
        if (size != null) {
            if (format != null) {
                args = String.format("{\"path\":\"%s\",\"format\":{\".tag\":\"%s\"},\"size\":{\".tag\":\"%s\"}}", path, format, size);
            } else {
                args = String.format("{\"path\":\"%s\",\"size\":{\".tag\":\"%s\"}}", path, size);
            }
        } else if (rev != null) {
            cmd = "/download";
            args = String.format("{\"path\":\"%s\",\"rev\":\"%s\"}", path, rev);
        }

        Request dbxRequest = new Request.Builder()
                .cacheControl(new CacheControl.Builder().noStore().build())
                .header(REPO_HEADER_AUTH, auth)
                .header(REPO_HEADER_ARG, args)
                .method("POST", RequestBody.create(MediaType.parse(""), ""))
                .get()
                .build();
        return dbxRequest;
    }

    @Override
    public void fetch(final OkHttpNetworkFetchState fetchState, final Callback callback) {
        fetchState.submitTime = SystemClock.elapsedRealtime();
        final Uri uri = fetchState.getUri();
        // TODO: check uri.scheme and uri.authority to detect if request should be intercepted

        final Request request = buildRequest(uri);
        final Call call = mOkHttpClient.newCall(request);

        fetchState.getContext().addCallbacks(
                new BaseProducerContextCallbacks() {
                    @Override
                    public void onCancellationRequested() {
                        if (Looper.myLooper() != Looper.getMainLooper()) {
                            call.cancel();
                        } else {
                            mCancellationExecutor.execute(new Runnable() {
                                @Override
                                public void run() {
                                    call.cancel();
                                }
                            });
                        }
                    }
                });

        call.enqueue(
                new com.squareup.okhttp.Callback() {
                    @Override
                    public void onResponse(Response response) {
                        fetchState.responseTime = SystemClock.elapsedRealtime();
                        if (!response.isSuccessful()) {
                            handleException(call, new IOException("Unexpected HTTP code " + response), callback);
                            return;
                        }
                        final ResponseBody body = response.body();
                        try {
                            long contentLength = body.contentLength();
                            if (contentLength < 0) {
                                contentLength = 0;
                            }
                            callback.onResponse(body.byteStream(), (int) contentLength);
                        } catch (Exception e) {
                            handleException(call, e, callback);
                        } finally {
                            try {
                                body.close();
                            } catch (Exception e) {
                                FLog.w(TAG, "Exception when closing response body", e);
                            }
                        }
                    }

                    @Override
                    public void onFailure(final Request request, final IOException e) {
                        handleException(call, e, callback);
                    }
                });
    }

    @Override
    public boolean shouldPropagate(OkHttpNetworkFetchState fetchState) {
        return true;
    }

    @Override
    public void onFetchCompletion(OkHttpNetworkFetchState fetchState, int byteSize) {
        fetchState.fetchCompleteTime = SystemClock.elapsedRealtime();
    }

    @Override
    public Map<String, String> getExtraMap(OkHttpNetworkFetchState fetchState, int byteSize) {
        Map<String, String> extraMap = new HashMap<>(4);
        extraMap.put(QUEUE_TIME, Long.toString(fetchState.responseTime - fetchState.submitTime));
        extraMap.put(FETCH_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.responseTime));
        extraMap.put(TOTAL_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.submitTime));
        extraMap.put(IMAGE_SIZE, Integer.toString(byteSize));
        return extraMap;
    }

    /**
     * Handles exceptions.
     * <p/>
     * <p> OkHttp notifies callers of cancellations via an IOException. If IOException is caught
     * after request cancellation, then the exception is interpreted as successful cancellation
     * and onCancellation is called. Otherwise onFailure is called.
     */
    private void handleException(final Call call, final Exception e, final Callback callback) {
        if (call.isCanceled()) {
            callback.onCancellation();
        } else {
            callback.onFailure(e);
        }
    }
}