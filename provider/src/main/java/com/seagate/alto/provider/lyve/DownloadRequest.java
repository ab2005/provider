/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DownloadRequest {
    @SerializedName("path")
    @Expose
    public final String path;

    public DownloadRequest(String path) {
        this.path = path;
    }
}
