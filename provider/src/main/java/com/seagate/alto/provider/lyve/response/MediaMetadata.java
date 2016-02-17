
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.response;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.seagate.alto.provider.lyve.LyveCloudProvider;
import com.seagate.alto.provider.Provider;

import java.util.Date;

public class MediaMetadata implements Provider.MediaMetadata {

    @SerializedName(".tag")
    @Expose
    public String Tag;
    @SerializedName("dimensions")
    @Expose
    public Dimensions dimensions;
    @SerializedName("location")
    @Expose
    public Location location;
    @SerializedName("time_taken")
    @Expose
    public String timeTaken;
    @SerializedName("duration")
    @Expose
    public Integer duration;

    @Override
    public Provider.Size dimensions() {
        return new Provider.Size(dimensions.width, dimensions.height);
    }

    @Override
    public double latitude() {
        return location.latitude;
    }

    @Override
    public double longitude() {
        return location.longitude;
    }

    @Override
    public Date timeTaken() {
        return LyveCloudProvider.dateFromString(timeTaken);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
