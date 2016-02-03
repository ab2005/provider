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


curl -X POST \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer MywxLE1JV0NkVHNoOG52QzdzaEl2R3dMSGc9PSx5UkRYS3JiQS9zUUR0em44ZzF1bDZXeC9DREQ2VTNnTlhsd2NwVkhZRk1QN0lGdHBIQ3NraktHdWFWOUVsUXVDMVUyV3plVTM5QWxJZms2ZHZLVGhTVkhnTllzSXovTlB4bFdQcU1XWFBzN1RFSU9DazU5MDYxNlluc0dHd3FzTmhxTTNlSFhNU0FIQlI1REx6VWd6QURnMG5WSHdlZ3M4YkFFOVprY2sxYmFSVUMvUzMzTitrWnQ3TFgvTHoyYmV2V3ZQbHdxQk5neEZKTy9Zb0NnekNUS1phVkQ4a0VIOEhvL0IzM0crWTU5bU54dXk5eTh2d3VBc2tEN1hkdHQ0djdvUzNIUS9KSWJHYUlnT015c2N5TXRtWUowbG50N0syTnpCQVdTdWJOcz0=' \
--header 'Accept: application/json' -d '{
  "path": "/d6f14c1e-ce88-4ebf-aa2f-f50fc7250dc4/Demo1",
  "limit": 1000,
  "include_deleted": true,
  "include_media_info": true,
  "include_child_count": true
}' 'https://api.dogfood.blackpearlsystems.net/v1/files/list_folder' | pjson

curl -X POST \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer MywxLE1JV0NkVHNoOG52QzdzaEl2R3dMSGc9PSx5UkRYS3JiQS9zUUR0em44ZzF1bDZXeC9DREQ2VTNnTlhsd2NwVkhZRk1QN0lGdHBIQ3NraktHdWFWOUVsUXVDMVUyV3plVTM5QWxJZms2ZHZLVGhTVkhnTllzSXovTlB4bFdQcU1XWFBzN1RFSU9DazU5MDYxNlluc0dHd3FzTmhxTTNlSFhNU0FIQlI1REx6VWd6QURnMG5WSHdlZ3M4YkFFOVprY2sxYmFSVUMvUzMzTitrWnQ3TFgvTHoyYmV2V3ZQbHdxQk5neEZKTy9Zb0NnekNUS1phVkQ4a0VIOEhvL0IzM0crWTU5bU54dXk5eTh2d3VBc2tEN1hkdHQ0djdvUzNIUS9KSWJHYUlnT015c2N5TXRtWUowbG50N0syTnpCQVdTdWJOcz0=' \
--header 'Accept: application/json' -d '{
  "path": "/d6f14c1e-ce88-4ebf-aa2f-f50fc7250dc4/Demo1/test",
  "limit": 1000,
  "include_deleted": true,
  "include_media_info": true,
  "include_child_count": true
}' 'https://api.dogfood.blackpearlsystems.net/v1/files/list_folder' | pjson


curl -X POST \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer MywxLE1JV0NkVHNoOG52QzdzaEl2R3dMSGc9PSx5UkRYS3JiQS9zUUR0em44ZzF1bDZXeC9DREQ2VTNnTlhsd2NwVkhZRk1QN0lGdHBIQ3NraktHdWFWOUVsUXVDMVUyV3plVTM5QWxJZms2ZHZLVGhTVkhnTllzSXovTlB4bFdQcU1XWFBzN1RFSU9DazU5MDYxNlluc0dHd3FzTmhxTTNlSFhNU0FIQlI1REx6VWd6QURnMG5WSHdlZ3M4YkFFOVprY2sxYmFSVUMvUzMzTitrWnQ3TFgvTHoyYmV2V3ZQbHdxQk5neEZKTy9Zb0NnekNUS1phVkQ4a0VIOEhvL0IzM0crWTU5bU54dXk5eTh2d3VBc2tEN1hkdHQ0djdvUzNIUS9KSWJHYUlnT015c2N5TXRtWUowbG50N0syTnpCQVdTdWJOcz0=' \
--header 'Accept: application/json' -d '{
  "path": "/d6f14c1e-ce88-4ebf-aa2f-f50fc7250dc4/Demo1/test/",
  "query": "jpg"
}' 'https://api.dogfood.blackpearlsystems.net/v1/files/search' | pjson

curl -X POST \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer MywxLE1JV0NkVHNoOG52QzdzaEl2R3dMSGc9PSx5UkRYS3JiQS9zUUR0em44ZzF1bDZXeC9DREQ2VTNnTlhsd2NwVkhZRk1QN0lGdHBIQ3NraktHdWFWOUVsUXVDMVUyV3plVTM5QWxJZms2ZHZLVGhTVkhnTllzSXovTlB4bFdQcU1XWFBzN1RFSU9DazU5MDYxNlluc0dHd3FzTmhxTTNlSFhNU0FIQlI1REx6VWd6QURnMG5WSHdlZ3M4YkFFOVprY2sxYmFSVUMvUzMzTitrWnQ3TFgvTHoyYmV2V3ZQbHdxQk5neEZKTy9Zb0NnekNUS1phVkQ4a0VIOEhvL0IzM0crWTU5bU54dXk5eTh2d3VBc2tEN1hkdHQ0djdvUzNIUS9KSWJHYUlnT015c2N5TXRtWUowbG50N0syTnpCQVdTdWJOcz0=' \
-d '{
        "path": "/d6f14c1e-ce88-4ebf-aa2f-f50fc7250dc4/Demo1/test/IMG_8591.JPG"
}' 'https://api.dogfood.blackpearlsystems.net/v1/files/download'

     */
}
