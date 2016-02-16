/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.imagepipeline;

import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.NetworkFetcher;

import java.io.IOException;
import java.util.concurrent.Executor;

import okhttp3.ResponseBody;

public class RetrofitCallbackHandler extends BaseProducerContextCallbacks implements retrofit2.Callback<okhttp3.ResponseBody> {
    private static final String TAG = RetrofitCallbackHandler.class.getSimpleName();

    private final NetworkFetcher.Callback callback;
    private final OkHttpNetworkFetcher.HttpNetworkFetchState fetchState;
    private final retrofit2.Call<ResponseBody> call;
    private final Executor executor;
    private final long start;

    public RetrofitCallbackHandler(retrofit2.Call<ResponseBody> call, Executor executoor, final OkHttpNetworkFetcher.HttpNetworkFetchState fetchState, final NetworkFetcher.Callback callback) {
        this.callback = callback;
        this.fetchState = fetchState;
        this.call = call;
        fetchState.getContext().addCallbacks(this);
        this.executor = executoor;
        this.start = SystemClock.elapsedRealtime();
    }

    @Override
    public void onResponse(final retrofit2.Call<ResponseBody> call, final retrofit2.Response<ResponseBody> response) {
        fetchState.responseTime = SystemClock.elapsedRealtime();
        if (response.isSuccess()) {
            try {
                long contentLength = response.body().contentLength();
                Log.d(TAG,"onResponse(): length = " + contentLength + " - response time = " +
                        (fetchState.responseTime - fetchState.submitTime) + "/"
                        + (fetchState.responseTime - start) +  ": " +  fetchState.getUri());

                long t = SystemClock.elapsedRealtime();
                callback.onResponse(response.body().byteStream(), -1);
                Log.d(TAG, "callback time = " + (SystemClock.elapsedRealtime() - t) + ": " +  fetchState.getUri());
            } catch (IOException e) {
                onFailure(call, e);
            } finally {
                response.body().close();
            }
        } else {
            Log.d(TAG, "not success, " + (fetchState.responseTime - fetchState.submitTime) + ": " +  fetchState.getUri());
            String errMessage = response.code() + ", " + response.message();
            onFailure(call, new IOException("Request failed!" + errMessage));
        }
    }

    @Override
    public void onFailure(retrofit2.Call<ResponseBody> call, Throwable t) {
        long t0 = SystemClock.elapsedRealtime();
        Log.d(TAG, "onFailure() " + t.getMessage() + " after " + (t0 - start) + ": " + fetchState.getUri());
        if (call.isCanceled()) {
            callback.onCancellation();
        } else {
            callback.onFailure(t);
        }
    }

    @Override
    public void onCancellationRequested() {
        long t0 = SystemClock.elapsedRealtime();
        Log.d(TAG, "onCancelationRequest() after " + (t0 - start) + ": " + fetchState.getUri());
        if (Looper.myLooper() != Looper.getMainLooper()) {
            call.cancel();
        } else {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    call.cancel();
                }
            });
        }
    }
}
