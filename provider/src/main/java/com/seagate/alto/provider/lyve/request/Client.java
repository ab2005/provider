
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Client {

    @SerializedName("client_id")
    @Expose
    public String clientId;
    @SerializedName("client_platform")
    @Expose
    public String clientPlatform;
    @SerializedName("client_type")
    @Expose
    public String clientType;
    @SerializedName("client_version")
    @Expose
    public String clientVersion;
    @SerializedName("display_name")
    @Expose
    public String displayName;

    public Client withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public Client withClientPlatform(String clientPlatform) {
        this.clientPlatform = clientPlatform;
        return this;
    }

    public Client withClientType(String clientType) {
        this.clientType = clientType;
        return this;
    }

    public Client withClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
        return this;
    }

    public Client withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

}
