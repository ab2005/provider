package com.seagate.alto.provider.lyve.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.seagate.alto.provider.lyve.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ListFolderRequest {

    @SerializedName("path")
    @Expose
    public String path;
    @SerializedName("limit")
    @Expose
    public Integer limit;
    @SerializedName("include_deleted")
    @Expose
    public Boolean includeDeleted;
    @SerializedName("include_media_info")
    @Expose
    public Boolean includeMediaInfo;
    @SerializedName("include_child_count")
    @Expose
    public Boolean includeChildCount;

    public ListFolderRequest withPath(String path) {
        this.path = path;
        return this;
    }

    public ListFolderRequest withLimit(Integer limit) {
        this.limit = limit;
        return this;
    }

    public ListFolderRequest withIncludeDeleted(Boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
        return this;
    }

    public ListFolderRequest withIncludeMediaInfo(Boolean includeMediaInfo) {
        this.includeMediaInfo = includeMediaInfo;
        return this;
    }

    public ListFolderRequest withIncludeChildCount(Boolean includeChildCount) {
        this.includeChildCount = includeChildCount;
        return this;
    }

}