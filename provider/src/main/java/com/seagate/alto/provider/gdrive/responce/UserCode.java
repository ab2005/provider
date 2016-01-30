/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.gdrive.responce;

import com.google.gson.annotations.SerializedName;

/**
 * Serialized responce to get user code.
 */
public class UserCode extends BaseResponse {

    @SerializedName("device_code")
    private String deviceCode;

    @SerializedName("user_code")
    private String userCode;

    @SerializedName("verification_url")
    private String verificationUrl;

    @SerializedName("expires_in")
    private Long expiresIn;

    private Integer interval;

    public String getDeviceCode() {
        return deviceCode;
    }

    public String getUserCode() {
        return userCode;
    }

    public String getVerificationUrl() {
        return verificationUrl;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public Integer getInterval() {
        return interval;
    }

    @Override
    public String toString() {
        if (super.getError() != null) {
            return "UserCode{error='" + super.getError() + "'}";
        }

        return "UserCode{" +
                "deviceCode='" + deviceCode + '\'' +
                ", userCode='" + userCode + '\'' +
                ", verificationUrl='" + verificationUrl + '\'' +
                ", expiresIn=" + expiresIn +
                ", interval=" + interval +
                '}';
    }
}

/*
AAEAA765-0AA9-40B6-B414-CE723B70F07F
curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
  "email": "ab2005@gmail.com",
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
}' 'https://api.test.blackpearlsystems.net/v1/users/create_account'


curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
  "email": "ab2005@gmail.com",
  "password": "1111111",
  "client": {
    "client_id": "AAEAA765-0AA9-40B6-B414-CE723B70F07F",
    "client_platform": "android",
    "client_type": "phone",
    "client_version": "0.0.1",
    "display_name": "d"
  }
}' 'https://api.test.blackpearlsystems.net/v1/auth/login'  | pjson

curl -X POST
--header 'Content-Type: application/json'
--header 'Accept: application/json' -d '{
  "path": "/",
  "limit": 0,
  "include_deleted": false,
  "include_media_info": false
}' 'https://api.test.blackpearlsystems.net/v1/files/list_folder'


curl -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' -d '{
  "email": "a1111111@1111111b.com",
  "password": "1111111",
  "name": {
    "given_name": "testing alto",
    "surname": "a",
    "familiar_name": "b",
    "display_name": "d"
  },
  "client": {
    "client_id": "BF947BDF-38B5-4E69-B20E-9C62FD28CE1F",
    "client_platform": "android",
    "client_type": "phone",
    "client_version": "0.0.1",
    "display_name": "d"
  },
  "is_internal": true
}' 'https://api.test.blackpearlsystems.net/v1/users/create_account' | pjson

















 */