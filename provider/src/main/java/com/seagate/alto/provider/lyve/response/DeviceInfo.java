
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DeviceInfo {

    @SerializedName("device_id")
    @Expose
    public String deviceId;
    @SerializedName("device_make")
    @Expose
    public String deviceMake;
    @SerializedName("device_model")
    @Expose
    public String deviceModel;
    @SerializedName("created")
    @Expose
    public String created;
    @SerializedName("device_size")
    @Expose
    public Long deviceSize;
    @SerializedName("container_id")
    @Expose
    public String containerId;
    @SerializedName("expiration")
    @Expose
    public String expiration;
    @SerializedName("display_name")
    @Expose
    public String displayName;

}
