/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import com.facebook.stetho.okhttp3.StethoInterceptor;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
    public static final String API_BASE_URL = "https://api.dogfood.blackpearlsystems.net";
    public static final String DBX_API_BASE_URL = "https://api.dropboxapi.com";
    private static final long READ_TIMEOUT_SEC = 20;
    private final static int MAX_CONNECTIONS = 3;

    public static LyveCloudClient createLyveCloudService (String authToken) {
        return createService(API_BASE_URL, LyveCloudClient.class, authToken);
    }

    public static DbxCloudClient createDropboxService (String authToken) {
        return createService(API_BASE_URL, DbxCloudClient.class, authToken);
    }

    /**
     * A factory method to create a service instance with authorization
     */
    public static <S> S createService(String baseUrl, Class<S> serviceClass, final String authToken) {
        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder()
//                .connectionPool(new ConnectionPool(MAX_CONNECTIONS, 5, TimeUnit.MINUTES))
                .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
                .addInterceptor(new StethoInterceptor());
//                .sslSocketFactory(SSLConfig.getSSLSocketFactory());

        if (authToken != null) {
            httpClientBuilder.addInterceptor(new Interceptor() {
                @Override
                public Response intercept(Interceptor.Chain chain) throws IOException {
                    Request original = chain.request();
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("Authorization", "Bearer " + authToken)
                            .method(original.method(), original.body());

                    Request request = requestBuilder.build();
                    return chain.proceed(request);
                }
            });
        }
        OkHttpClient client = httpClientBuilder.build();
        client.dispatcher().setMaxRequestsPerHost(MAX_CONNECTIONS);
        client.dispatcher().setMaxRequests(MAX_CONNECTIONS * 2);
        Retrofit.Builder builder =
                new Retrofit.Builder()
                        .baseUrl(baseUrl)
                        .callbackExecutor(Executors.newCachedThreadPool())
                        .addConverterFactory(GsonConverterFactory.create());
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }

}