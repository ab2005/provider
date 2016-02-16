
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.imagepipeline;

import android.net.Uri;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseNetworkFetcher;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.seagate.alto.provider.DbxProvider;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.Providers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import okhttp3.CacheControl;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Network fetcher that uses OkHttp and Provider callbacks as a backend for image request calls.
 */
public class OkHttpNetworkFetcher extends BaseNetworkFetcher<OkHttpNetworkFetcher.HttpNetworkFetchState> {

    public static class HttpNetworkFetchState extends FetchState {
        public long submitTime;
        public long responseTime;
        public long fetchCompleteTime;

        /**
         * Used for testing.
         */
        public HttpNetworkFetchState() {
            this(null, null);
        }

        public HttpNetworkFetchState(Consumer<EncodedImage> consumer, ProducerContext producerContext) {
            super(consumer, producerContext);
        }
    }

    /**
     * A runner for cancelable network requests.
     */
    public interface Call extends Runnable {
        void cancel();
    }

    private static final String TAG = OkHttpNetworkFetcher.class.getSimpleName();

    private static final String QUEUE_TIME = "queue_time";
    private static final String FETCH_TIME = "fetch_time";
    private static final String TOTAL_TIME = "total_time";
    private static final String IMAGE_SIZE = "image_size";

    private final OkHttpClient mOkHttpClient;

    private final Executor mExecutor;

    /**
     * Image fetcher that uses client's executor to handle provider calls.
     * @param okHttpClient client to use
     */
    public OkHttpNetworkFetcher(OkHttpClient okHttpClient) {
        mOkHttpClient = okHttpClient;
        mExecutor = okHttpClient.dispatcher().executorService();
    }

    @Override
    public HttpNetworkFetchState createFetchState(Consumer<EncodedImage> consumer, ProducerContext context) {
        return new HttpNetworkFetchState(consumer, context);
    }

    @Override
    public void fetch(final HttpNetworkFetchState fetchState, final Callback callback) {
        fetchState.submitTime = SystemClock.elapsedRealtime();
        final Uri uri = fetchState.getUri();
        Log.d(TAG, uri + " fetch...");
        final String domain = fetchState.getUri().getAuthority();
        switch (domain) {
            case "seagate":
                fetchLyveCloudProvider(fetchState, callback);
//                executeCall(fetchState, new LyveCloudFilesCall(
//                        fetchState, callback, Providers.SEAGATE.provider, mOkHttpClient));
                break;
            case "dropbox":
                executeCall(fetchState, new DbxFilesCall(
                                fetchState, callback, (DbxProvider) Providers.DROPBOX.provider));
                break;
            default:
                final Request request = new Request.Builder()
                        .cacheControl(new CacheControl.Builder().noStore().build())
                        .url(uri.toString())
                        .get()
                        .build();
                final okhttp3.Call call = mOkHttpClient.newCall(request);
                call.enqueue(new OkHttpCallbackHandler(call, mExecutor, fetchState, callback));
                break;
        }
    }

    @Override
    public void onFetchCompletion(HttpNetworkFetchState fetchState, int byteSize) {
        fetchState.fetchCompleteTime = SystemClock.elapsedRealtime();
        final Uri uri = fetchState.getUri();
        Log.d(TAG, uri + " compleeted. " + TOTAL_TIME + " = " + Long.toString(fetchState.fetchCompleteTime - fetchState.submitTime));
    }

    @Override
    public Map<String, String> getExtraMap(HttpNetworkFetchState fetchState, int byteSize) {
        Map<String, String> extraMap = new HashMap<>(4);
        extraMap.put(QUEUE_TIME, Long.toString(fetchState.responseTime - fetchState.submitTime));
        extraMap.put(FETCH_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.responseTime));
        extraMap.put(TOTAL_TIME, Long.toString(fetchState.fetchCompleteTime - fetchState.submitTime));
        extraMap.put(IMAGE_SIZE, Integer.toString(byteSize));
        return extraMap;
    }

    // Enqueues retrofit callback handler to provider service callback thread
    private void fetchLyveCloudProvider(final HttpNetworkFetchState fetchState, final Callback callback) {
        Uri uri = fetchState.getUri();
        String path = uri.getPath();
        path = path.startsWith("//") ? uri.getPath().substring(1) : path;
        try {
            final retrofit2.Call<okhttp3.ResponseBody> call = Providers.SEAGATE.provider.download(path);
            call.enqueue(new RetrofitCallbackHandler(call, mExecutor, fetchState, callback));
        } catch (Provider.ProviderException e) {
            callback.onFailure(e);
        }
    }

    private void executeCall(final HttpNetworkFetchState fetchState, final Call call) {
        fetchState.getContext().addCallbacks(new BaseProducerContextCallbacks() {
            @Override
            public void onCancellationRequested() {
                if (Looper.myLooper() != Looper.getMainLooper()) {
                    call.cancel();
                } else {
                    mExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            call.cancel();
                        }
                    });
                }
            }
        });

        mExecutor.execute(call);
    }
}
