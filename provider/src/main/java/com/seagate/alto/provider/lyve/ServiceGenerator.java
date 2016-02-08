/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ServiceGenerator {
    public static final String API_BASE_URL = "https://api.dogfood.blackpearlsystems.net";
    private static OkHttpClient.Builder sHttpClient = new OkHttpClient.Builder();

    final static class StreamBodyConverter<T extends ResponseBody> implements Converter<ResponseBody, T> {
        @Override public T convert(ResponseBody value) throws IOException {
            System.out.println("....");
            LyveCloudClient.Stream body = new LyveCloudClient.Stream(value);
            return (T) body.get();
        }
    }

    final static class StreamConverterFactory extends Converter.Factory {
        @Override
        public Converter<okhttp3.ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
            if (!(type instanceof Class<?>)) {
                return null;
            }
            Class<?> c = (Class<?>) type;
            if (!LyveCloudClient.Stream.class.isAssignableFrom(c)) {
                return null;
            }

            return new StreamBodyConverter();
        }
    }

    private static Retrofit.Builder jsonBuilder =
            new Retrofit.Builder()
                    .baseUrl(API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create());
    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(new StreamConverterFactory())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(API_BASE_URL);

    /**
     * A factory method to create unauthorized service instance
     */
    public static <S> S createJsonService(Class<S> serviceClass) {
        Retrofit retrofit = jsonBuilder.client(sHttpClient.build()).build();
        return retrofit.create(serviceClass);
    }

    /**
     * A factory method to create a Json service instance with auto authorization
     */
    public static <S> S createJsonService(Class<S> serviceClass, final String authToken) {
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
        Retrofit retrofit = jsonBuilder.client(client).build();
        return retrofit.create(serviceClass);
    }

    /**
     * A factory method to create a Json service instance with auto authorization
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