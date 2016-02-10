/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.gdrive.service;

import com.seagate.alto.provider.gdrive.responce.Profile;

import retrofit2.http.GET;

public interface GoogleApisService {
    public static final String BASE_URL = "https://www.googleapis.com";

    @GET("/oauth2/v1/userinfo?alt=json")
    Profile getProfile();
}
