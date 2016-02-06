/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.seagate.alto.provider.lyve.LyveCloudClient;
import com.seagate.alto.provider.lyve.ServiceGenerator;
import com.seagate.alto.provider.lyve.request.Client;
import com.seagate.alto.provider.lyve.request.ListFolderRequest;
import com.seagate.alto.provider.lyve.request.LoginRequest;
import com.seagate.alto.provider.lyve.request.SearchRequest;
import com.seagate.alto.provider.lyve.response.ListFolderResponse;
import com.seagate.alto.provider.lyve.response.SearchResponse;
import com.seagate.alto.provider.lyve.response.Token;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.GsonConverterFactory;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * LyveCloud {@link Provider} API implementation.
 */
public class LyveCloudProvider implements Provider {

    public static SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd'T'HHmmss.SSS'Z'");

    private static Date NO_DATE = new Date(1961, 5, 12);
    private static final String TAG = LyveCloudProvider.class.getName();
    private static final String DOMAIN = "seagate";
    public static final String API_BASE_URL = "https://api.dogfood.blackpearlsystems.net";
    private final OkHttpClient.Builder sHttpClient = new OkHttpClient.Builder();
    private final Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create());

    private final LyveCloudClient mLyveCloudClient;
    private String mAccessToken;

    /*package*/ LyveCloudProvider() {
        sHttpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();
                Request.Builder requestBuilder = original.newBuilder();
                String token = getAccessToken();
                if (token != null) {
                    requestBuilder.header("Authorization", "Bearer " + token);
                }
                requestBuilder.method(original.method(), original.body());
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        OkHttpClient client = sHttpClient.build();
        Retrofit retrofit = builder.client(client).build();
        mLyveCloudClient = retrofit.create(LyveCloudClient.class);
    }

    @Override
    public void setAccessToken(String token) {
        mAccessToken = token;
    }

    @Override
    public String getAccessToken() {
        return mAccessToken;
    }

    @Override
    public String getDomain() {
        return DOMAIN;
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
            Response<ListFolderResponse> response = mLyveCloudClient.listFolder(req).execute();
            ListFolderResponse lfr = response.body();
            return lfr;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
            Response<SearchResponse> response = mLyveCloudClient.search(req).execute();
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
            Response<SearchResponse> response = mLyveCloudClient.search(req).execute();
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

    @Override
    public Uri getThumbnailUri(String path, String size, String format) throws ProviderException {
        return getImageUri(path, format, size);
    }

    @Override
    public Uri getUri(String path, String rev) throws ProviderException {
        return getImageUri(path, null, null);
    }

    /**
     * A convenience method to login to Lyve Cloud
     *
     * @param user
     * @param password
     * @return access token string or null if login failed
     * @throws IOException
     */
    @Nullable public static String login(String user, String password) throws IOException {
        LoginRequest login = new LoginRequest()
                .withEmail(user)
                .withPassword(password)
                .withClient(new Client()
                        .withClientId("AAEAA765-0AA9-40B6-B414-CE723B70F07F")
                        .withDisplayName("alto-test-lyve")
                        .withClientPlatform("android")
                        .withClientType("phone")
                        .withClientVersion("0.0.1"));


        LyveCloudClient client = ServiceGenerator.createJsonService(LyveCloudClient.class);
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
            return null;
        }
    }

    /**
     * Get a date value from a string.
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
