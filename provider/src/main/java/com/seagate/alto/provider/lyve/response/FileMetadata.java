
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.response;

import android.net.Uri;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.seagate.alto.provider.LyveCloudProvider;
import com.seagate.alto.provider.Provider;

import java.util.Date;

public class FileMetadata implements Provider.FileMetadata, Provider.Metadata {
    @SerializedName("parent")
    @Expose
    public String parent;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("path_lower")
    @Expose
    public String pathLower;
    @SerializedName("name")
    @Expose
    public String name;
    @SerializedName(".tag")
    @Expose
    public String Tag;
    @SerializedName("child_count")
    @Expose
    public Integer childCount;
    @SerializedName("rev")
    @Expose
    public String rev;
    @SerializedName("size")
    @Expose
    public Long size;
    @SerializedName("is_deleted")
    @Expose
    public Boolean isDeleted;
    @SerializedName("filetype")
    @Expose
    public String filetype;
    @SerializedName("server_modified")
    @Expose
    public String serverModified;
    @SerializedName("client_modified")
    @Expose
    public String clientModified;
    @SerializedName("media_info")
    @Expose
    public MediaInfo mediaInfo;
    @SerializedName("device_info")
    @Expose
    public DeviceInfo deviceInfo;

    @Override
    public String id() {
        return id;
    }

    @Override
    public Date clientModified() {
        return new Date(clientModified);
    }

    @Override
    public Date serverModified() {
        return new Date(serverModified);
    }

    @Override
    public String rev() {
        return rev;
    }

    @Override
    public long size() {
        return size == null ? 0L : size;
    }

    @Override
    public Provider.MediaInfo mediaInfo() {
        return mediaInfo();
    }

    @Override
    public Uri imageUri() {
        return LyveCloudProvider.getImageUri(pathLower, null, null);
    }

    @Override
    public Uri thumbnailUri(String type, String size) {
        return LyveCloudProvider.getImageUri(pathLower, type, size);
    }

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
    public String toString() {
        return new Gson().toJson(this);
    }
}
