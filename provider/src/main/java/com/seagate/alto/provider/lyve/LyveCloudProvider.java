/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.facebook.imagepipeline.producers.NetworkFetcher;
import com.seagate.alto.provider.Provider;
import com.seagate.alto.provider.imagepipeline.OkHttpNetworkFetcher;
import com.seagate.alto.provider.imagepipeline.RetrofitCallbackHandler;
import com.seagate.alto.provider.lyve.request.Client;
import com.seagate.alto.provider.lyve.request.DownloadRequest;
import com.seagate.alto.provider.lyve.request.ListFolderRequest;
import com.seagate.alto.provider.lyve.request.LoginRequest;
import com.seagate.alto.provider.lyve.request.SearchRequest;
import com.seagate.alto.provider.lyve.response.ListFolderResponse;
import com.seagate.alto.provider.lyve.response.SearchResponse;
import com.seagate.alto.provider.lyve.response.Token;
import com.seagate.alto.provider.network.ServiceGenerator;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

/**
 * LyveCloud {@link Provider} API implementation.
 */
public class LyveCloudProvider implements Provider {
    private static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS'Z'");
    private static Date NO_DATE = new Date(1961, 5, 12);
    private static final String TAG = LyveCloudProvider.class.getName();
    private static final String DOMAIN = "seagate";

    private LyveCloudClient mRetrofit;
    private String mAccessToken;

    public LyveCloudProvider() {
    }

    @Override
    public void setAccessToken(String token) {
        mAccessToken = token;
        mRetrofit = ServiceGenerator.createLyveCloudService(token);
    }

    @Override
    public String getAccessToken() {
        return mAccessToken;
    }

    @Override
    public String getDomain() {
        return ServiceGenerator.API_BASE_URL;
    }

    @Override
    public FolderMetadata createFolder(@NonNull String path) throws ProviderException {
        // TODO:
        throw new UnsupportedOperationException();
    }

    @Override
    public ListFolderResult listFolder(@NonNull String path) throws ProviderException {
        ListFolderRequest req = new ListFolderRequest()
                .withPath(path)
                .withIncludeMediaInfo(true)
                .withIncludeChildCount(true)
                .withIncludeDeleted(true)
                .withLimit(1000);

        try {
            Call<ListFolderResponse> call = mRetrofit.listFolder(req);
            Response<ListFolderResponse> response = call.execute();
            if (!response.isSuccess()) {
                throw new ProviderException(response.message(), null);
            }
            return response.body();

        } catch (IOException e) {
            throw new ProviderException(e);
        }
    }

    @Override
    public ListFolderResult listFolderContinue(@NonNull String cursor) throws ProviderException {
        // TODO
        throw new UnsupportedOperationException();
    }

    @Override
    public SearchResult search(@NonNull String path, @NonNull String query) throws ProviderException {
        SearchRequest req = new SearchRequest()
                .withPath(path)
                .withQuery(query);
        try {
            Response<SearchResponse> response = mRetrofit.search(req).execute();
            SearchResponse sr = response.body();
            return sr;
        } catch (IOException e) {
            throw new ProviderException("search error!", e);
        }
    }

    @Override
    public SearchResult search(@NonNull String path, @NonNull String query, Long start, Long maxResults, SearchMode mode) throws ProviderException {
        String searchMode = (mode == SearchMode.filename) ? "filename" : "deleted_filename";
        SearchRequest req = new SearchRequest()
                .withPath(path)
                .withQuery(query)
                .withStart(start.intValue())
                .withMaxResults(maxResults.intValue())
                .withMode(searchMode);
        try {
            Response<SearchResponse> response = mRetrofit.search(req).execute();
            SearchResponse sr = response.body();
            return sr;
        } catch (IOException e) {
            throw new ProviderException("search error!", e);
        }
    }

    @Override
    public Metadata getMetadata(@NonNull String path, boolean includeMediaInfo) throws ProviderException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Metadata delete(@NonNull String path) throws ProviderException {
        throw new UnsupportedOperationException();
    }

    /**
     * Get a {@link Call} to download file at a given path.
     *
     * @param path
     */
    @Override
    public Call<ResponseBody> download(String path) throws ProviderException {
        return mRetrofit.download(new DownloadRequest(path));
    }

    /**
     * Download file at a given path.
     *
     * @param path
     */
    @Override
    public void download(String path, final NetworkFetcher.Callback cb) throws ProviderException {
        final Call<ResponseBody> call = download(path);
        OkHttpNetworkFetcher.HttpNetworkFetchState fs = new OkHttpNetworkFetcher.HttpNetworkFetchState();
        call.enqueue(new RetrofitCallbackHandler(call, AsyncTask.THREAD_POOL_EXECUTOR, fs, cb));
    }

    @Override
    public Uri getThumbnailUri(String path, String size, String format) throws ProviderException {
        return getImageUri(path, format, size);
    }

    @Override
    public Uri getUri(String path, String rev) throws ProviderException {
        return getImageUri(path, null, null);
    }

    /**
     * A helper to login to Lyve Cloud
     *
     * @param user
     * @param password
     * @return access token string or null if login failed
     * @throws IOException
     */
    @Nullable
    public static String login(String user, String password) throws IOException {
        LoginRequest login = new LoginRequest()
                .withEmail(user)
                .withPassword(password)
                .withClient(new Client()
                        .withClientId("AAEAA765-0AA9-40B6-B414-CE723B70F07F")
                        .withDisplayName("alto-test-lyve")
                        .withClientPlatform("android")
                        .withClientType("phone")
                        .withClientVersion("0.0.1"));


        LyveCloudClient client = ServiceGenerator.createService(ServiceGenerator.API_BASE_URL, LyveCloudClient.class, null);
        Response<Token> response = client.login(login).execute();

        if (!response.isSuccess()) {
            Log.d(TAG, "Failed to login: " + response);
            return null;
        }

        Token token = response.body();

        return token.token;
    }

    /**
     * Get an Uri to use with Fresco or another image library.
     *
     * @param path
     * @param format
     * @param size
     * @return
     */
    public static Uri getImageUri(@NonNull String path, @Nullable String format, @Nullable String size) {
        try {
            Uri.Builder ub = new Uri.Builder()
                    .scheme("http")
                    .authority(DOMAIN)
                    .appendPath(path);
            if (size != null) {
                ub.appendQueryParameter("size", size);
            }
            if (format != null) {
                ub.appendQueryParameter("format", format);
            }
            return ub.build();
        } catch (NullPointerException e) {
            return Uri.EMPTY;
        }
    }

    /**
     * Get a date value from a string.
     *
     * @return a valid {@link Date} or Gagarin day
     */
    public static Date dateFromString(String dateString) {
        try {
            return DATE_FORMATTER.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return NO_DATE;
    }
}
