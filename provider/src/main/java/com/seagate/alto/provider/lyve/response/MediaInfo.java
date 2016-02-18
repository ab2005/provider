
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.response;

import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.seagate.alto.provider.Provider;

public class MediaInfo implements Provider.MediaInfo {

    @SerializedName(".tag")
    @Expose
    public String Tag;
    @SerializedName("metadata")
    @Expose
    public MediaMetadata metadata;

    @Override
    public @Nullable Provider.MediaInfo.Tag tag() {
        switch(Tag) {
            case "metadata": return Provider.MediaInfo.Tag.metadata;
            case "pending": return Provider.MediaInfo.Tag.pending;
            default: return null;
        }
    }

    @Override
    public Provider.MediaMetadata metadata() {
        return metadata;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
