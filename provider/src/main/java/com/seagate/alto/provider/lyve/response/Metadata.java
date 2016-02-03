
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.response;

import android.net.Uri;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.seagate.alto.provider.Provider;

import java.util.Date;

public class Metadata implements Provider.FileMetadata {

    @SerializedName("parent")
    @Expose
    public String parent;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName("path_lower")
    @Expose
    public String pathLower;
    @SerializedName(".tag")
    @Expose
    public String Tag;
    @SerializedName("client_modified")
    @Expose
    public String clientModified;
    @SerializedName("server_modified")
    @Expose
    public String serverModified;
    @SerializedName("rev")
    @Expose
    public String rev;
    @SerializedName("size")
    @Expose
    public Integer size;

    @Override
    public String name() {
        return name;
    }

    @Override
    public String pathLower() {
        return pathLower;
    }

    @Override
    public String parentSharedFolderId() {
        return parent;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Date clientModified() {
        // FIXME: date format
        return new Date(clientModified);
    }

    @Override
    public Date serverModified() {
        // FIXME: date format
        return new Date(serverModified);
    }

    @Override
    public String rev() {
        return rev;
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    public Provider.MediaInfo mediaInfo() {
        return null;
    }

    @Override
    public Uri imageUri() {
        return null;
    }

    @Override
    public Uri thumbnailUri(String type, String size) {
        return null;
    }
}
