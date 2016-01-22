
package com.seagate.alto.provider.imagepipeline;

import android.net.Uri;
import android.os.Looper;
import android.os.SystemClock;

import com.dropbox.core.DbxDownloader;
import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxFiles;
import com.facebook.common.logging.FLog;
import com.facebook.imagepipeline.image.EncodedImage;
import com.facebook.imagepipeline.producers.BaseNetworkFetcher;
import com.facebook.imagepipeline.producers.BaseProducerContextCallbacks;
import com.facebook.imagepipeline.producers.Consumer;
import com.facebook.imagepipeline.producers.FetchState;
import com.facebook.imagepipeline.producers.ProducerContext;
import com.seagate.alto.provider.DbxProvider;
import com.seagate.alto.provider.Provider;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Network fetcher that uses OkHttp as a backend for Dropbox calls.
 */
public class OkHttpNetworkFetcher extends BaseNetworkFetcher<OkHttpNetworkFetcher.OkHttpNetworkFetchState> {

    private final Provider mProvider;

    public static class OkHttpNetworkFetchState extends FetchState {
        public long submitTime;
        public long responseTime;
        public long fetchCompleteTime;

        public OkHttpNetworkFetchState(
                Consumer<EncodedImage> consumer,
                ProducerContext producerContext) {
            super(consumer, producerContext);
        }
    }

    private static final String TAG = "OkHttpNetworkFetchProducer";
    private static final String QUEUE_TIME = "queue_time";
    private static final String FETCH_TIME = "fetch_time";
    private static final String TOTAL_TIME = "total_time";
    private static final String IMAGE_SIZE = "image_size";

    private final OkHttpClient mOkHttpClient;

    private final Executor mExecutor;

    /**
     * @param okHttpClient client to use
     */
    public OkHttpNetworkFetcher(OkHttpClient okHttpClient, Provider provider) {
        mOkHttpClient = okHttpClient;
        mExecutor = okHttpClient.getDispatcher().getExecutorService();
        mProvider = provider;
    }

    @Override
    public OkHttpNetworkFetchState createFetchState(
            Consumer<EncodedImage> consumer,
            ProducerContext context) {
        return new OkHttpNetworkFetchState(consumer, context);
    }

    static interface Call extends Runnable {
        void cancel();
    }

    /**
     * Handles exceptions from {@link com.squareup.okhttp.Call}.
     * <p/>
     * <p> OkHttp notifies callers of cancellations via an IOException. If IOException is caught
     * after request cancellation, then the exception is interpreted as successful cancellation
     * and onCancellation is called. Otherwise onFailure is called.
     */
    private void handleException(final com.squareup.okhttp.Call call, final Exception e, final Callback callback) {
        if (call.isCanceled()) {
            callback.onCancellation();
        } else {
            callback.onFailure(e);
        }
    }

    private void fetchUnknownProvider(final OkHttpNetworkFetchState fetchState, final Callback callback) {
        fetchState.submitTime = SystemClock.elapsedRealtime();
        final Uri uri = fetchState.getUri();
        final Request request = new Request.Builder()
                .cacheControl(new CacheControl.Builder().noStore().build())
                .url(uri.toString())
                .get()
                .build();
        final com.squareup.okhttp.Call call = mOkHttpClient.newCall(request);

        fetchState.getContext().addCallbacks(
                new BaseProducerContextCallbacks() {
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

        call.enqueue(
                new com.squareup.okhttp.Callback() {
                    @Override
                    public void onResponse(Response response) {
                        fetchState.responseTime = SystemClock.elapsedRealtime();
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

    private class DbxFilesCall implements Call {
        private final OkHttpNetworkFetchState fetchState;
        private final Callback callback;
        private DbxDownloader<DbxFiles.FileMetadata> downloader = null;

        DbxFilesCall(final OkHttpNetworkFetchState fetchState, final Callback callback) {
            this.fetchState = fetchState;
            this.callback = callback;
        }

        @Override
        public void run() {
            Uri uri = fetchState.getUri();
            final String path = uri.getPath();
            String strFormat = uri.getQueryParameter("format");
            String strSize = uri.getQueryParameter("size");
            DbxFiles.ThumbnailFormat format = DbxFiles.ThumbnailFormat.jpeg;
            DbxFiles.ThumbnailSize size = DbxFiles.ThumbnailSize.w128h128;

            if ("png".equalsIgnoreCase(strFormat)) {
                format = DbxFiles.ThumbnailFormat.png;
            }

            if ("w32h32".equals(strSize)) {
                size = DbxFiles.ThumbnailSize.w32h32;
            } else if ("w64h64".equals(strSize)) {
                size = DbxFiles.ThumbnailSize.w64h64;
            } else if ("w128h128".equals(strSize)) {
                size = DbxFiles.ThumbnailSize.w128h128;
            } else if ("w640h480".equals(strSize)) {
                size = DbxFiles.ThumbnailSize.w640h480;
            } else if ("w1024h768".equals(strSize)) {
                size = DbxFiles.ThumbnailSize.w1024h768;
            }

            DbxFiles mFilesClient = ((DbxProvider) mProvider).getFilesClient();

            // TODO: separate thumbnail download from full size image download
            try {
                downloader = mFilesClient.getThumbnailBuilder(path).format(format).size(size).start();
                fetchState.responseTime = SystemClock.elapsedRealtime();
                callback.onResponse(downloader.body, (int) downloader.result.size);
            } catch (DbxException | IOException e) {
                if (downloader != null) {
                    try {
                        downloader.close();
                    } catch (IllegalStateException ex) {
                        // ignore
                    }
                    downloader = null;
                    callback.onFailure(e);
                }
            }
        }

        @Override
        public void cancel() {
            if (downloader != null) {
                try {
                    downloader.close();
                } catch (IllegalStateException e) {
                    // nothing
                }
                downloader = null;
                callback.onCancellation();
            }
        }
    }

    private Call buildCall(final OkHttpNetworkFetchState fetchState, final Callback callback) {
        final Uri uri = fetchState.getUri();
        final String domain = uri.getAuthority();

        // TODO: add providers
        switch(domain) {
            case "seagate": return new DbxFilesCall(fetchState, callback);
            case "dropbox": return new DbxFilesCall(fetchState, callback);
            default: return null;
        }
    }

    @Override
    public void fetch(final OkHttpNetworkFetchState fetchState, final Callback callback) {
        fetchState.submitTime = SystemClock.elapsedRealtime();
        final Uri uri = fetchState.getUri();

        final Call call = buildCall(fetchState, callback);

        if (call != null) {
            mExecutor.execute(call);
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
        } else {
            fetchUnknownProvider(fetchState, callback);
        }
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
}
