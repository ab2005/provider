/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import com.seagate.alto.provider.lyve.request.ListFolderRequest;
import com.seagate.alto.provider.lyve.request.LoginRequest;
import com.seagate.alto.provider.lyve.request.SearchRequest;
import com.seagate.alto.provider.lyve.response.ListFolderResponse;
import com.seagate.alto.provider.lyve.response.SearchResponse;
import com.seagate.alto.provider.lyve.response.Token;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface LyveCloudClient {
    @POST("/v1/auth/login")
    Call<Token> login(@Body LoginRequest req);

    @POST("/v1/files/list_folder")
    Call<ListFolderResponse> listFolder(@Body ListFolderRequest req);

    @POST("/v1/files/search")
    Call<SearchResponse> search(@Body SearchRequest req);

//    @POST("/v1/files/download")
//    Call<SearchResponse> download(@Body DownloadRequest req);

}
