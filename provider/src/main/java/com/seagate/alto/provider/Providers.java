/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.stetho.Stetho;
import com.seagate.alto.provider.imagepipeline.ImagePipelineConfigFactory;

/**
 * The collection of available providers.
 */
public enum Providers {
    DROPBOX(new DbxProvider()),
    SEAGATE(new LyveCloudProvider());

    public final Provider provider;

    Providers(Provider provider) {
        this.provider = provider;
    }

    // TODO: add option to provide config

    /**
     *
     * A single initialization step which occurs once in your Application class {@link Application#onCreate()}.
     * @param context
     */
    public static void initWithDefaults(@NonNull Context context) {
        ImagePipelineConfig config = ImagePipelineConfigFactory.getOkHttpImagePipelineConfig(context);
        Fresco.initialize(context, config);
        Stetho.initializeWithDefaults(context);
    }

    /**
     * Load access tokens stored in the context and set providers.
     * @param context
     */
    public static void setStoredTokens(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        for (Providers p : Providers.values()) {
            String key = "Providers.access.token." + p.provider.getDomain();
            String token = prefs.getString(key, null);
            if (token != null) {
                p.provider.setAccessToken(token);
            }
        }
    }

    /**
     * Save access tokens from providers.
     * @param context
     */
    public static void storeTokens(Context context) {
        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
        edit.commit();
        for (Providers p : Providers.values()) {
            String key = "Providers.access.token." + p.provider.getDomain();
            String token = p.provider.getAccessToken();
            edit.putString(key, token);
        }
        edit.commit();
    }
}
