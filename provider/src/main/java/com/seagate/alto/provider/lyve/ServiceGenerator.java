/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

public class ServiceGenerator {
    public static final String API_BASE_URL = "https://api.dogfood.blackpearlsystems.net";
    private static OkHttpClient.Builder sHttpClient = new OkHttpClient.Builder();
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());

    /**
     * A factory method to create unauthorized service instance
     */
    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(sHttpClient.build()).build();
        return retrofit.create(serviceClass);
    }

    /**
     * A factory method to create a service instance with auto authorization
     */
    public static <S> S createService(Class<S> serviceClass, final String authToken) {
        if (authToken != null) {
            sHttpClient.addInterceptor(new Interceptor() {
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
        OkHttpClient client = sHttpClient.build();
        Retrofit retrofit = builder.client(client).build();
        return retrofit.create(serviceClass);
    }
}