/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class MatchType {
    @SerializedName(".tag")
    @Expose
    public String Tag;
}

