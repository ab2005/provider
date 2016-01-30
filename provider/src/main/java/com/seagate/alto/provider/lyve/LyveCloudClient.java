/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import com.seagate.alto.provider.gdrive.responce.AccessToken;
import com.seagate.alto.provider.gdrive.responce.UserCode;

import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface LyveCloudClient {
    String BASE_URL = "https://accounts.google.com";
    String ACCESS_GRANT_TYPE = "http://oauth.net/grant_type/device/1.0";
    String REFRESH_GRANT_TYPE = "refresh_token";

    @POST("/o/oauth2/device/code")
    @FormUrlEncoded
    UserCode getUserCode(@Field("client_id") String clientId,
                         @Field("scope") String scope);

    @POST("/o/oauth2/token")
    @FormUrlEncoded
    AccessToken getAccessToken(@Field("client_id") String clientId,
                               @Field("client_secret") String clientSecret,
                               @Field("code") String code,
                               @Field("grant_type") String grantType);

    @POST("/o/oauth2/token")
    @FormUrlEncoded
    AccessToken refreshAccessToken(@Field("client_id") String clientId,
                                   @Field("client_secret") String clientSecret,
                                   @Field("refresh_token") String refreshToken,
                                   @Field("grant_type") String grantType);

    // TODO:
    // create account

/*
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
  "email": "ab1@gmail.com",
  "password": "1111111",
  "name": {
    "given_name": "testing alto",
    "surname": "a",
    "familiar_name": "b",
    "display_name": "d"
  },
  "client": {
    "client_id": "AAEAA765-0AA9-40B6-B414-CE723B70F07F",
    "client_platform": "android",
    "client_type": "phone",
    "client_version": "0.0.1",
    "display_name": "d"
  },
  "is_internal": true
}' 'https://api.dogfood.blackpearlsystems.net/v1/users/create_account' | pjson

curl -v -c cook -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
  "email": "demo.zzz@seagate.com",
  "password": "demozzz",
  "client": {
    "client_id": "AAEAA765-0AA9-40B6-B414-CE723B70F07F",
    "client_platform": "android",
    "client_type": "phone",
    "client_version": "0.0.1",
    "display_name": "d"
  }
}' 'https://api.dogfood.blackpearlsystems.net/v1/auth/login'  | pjson

     */
}
