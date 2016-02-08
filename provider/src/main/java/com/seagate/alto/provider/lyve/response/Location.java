
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.response;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("latitude")
    @Expose
    public Integer latitude;
    @SerializedName("longitude")
    @Expose
    public Integer longitude;

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

}
