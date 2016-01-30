/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import okhttp3.OkHttpClient;
import retrofit2.GsonConverterFactory;
import retrofit2.Retrofit;

public class ServiceGenerator {
    public static final String API_BASE_URL = "https://api.dogfood.blackpearlsystems.net/v1";
    private static OkHttpClient.Builder sHttpClient = new OkHttpClient.Builder();
    private static Retrofit.Builder builder = new Retrofit.Builder().baseUrl(API_BASE_URL).addConverterFactory(GsonConverterFactory.create());

    /**
     * Factory method to create service instance
     * @param serviceClass
     * @param <S>
     * @return
     */
    public static <S> S createService(Class<S> serviceClass) {
        Retrofit retrofit = builder.client(sHttpClient.build()).build();
        return retrofit.create(serviceClass);
    }

    void main() {

    }
}