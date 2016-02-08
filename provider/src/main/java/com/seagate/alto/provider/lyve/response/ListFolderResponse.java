
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.response;

import com.google.gson.Gson;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.seagate.alto.provider.Provider;

import java.util.ArrayList;
import java.util.List;

public class ListFolderResponse implements Provider.ListFolderResult {

    @SerializedName("parent")
    @Expose
    public String parent;
    @SerializedName("id")
    @Expose
    public String id;
    @SerializedName("entries")
    @Expose
    public List<FileMetadata> entries = new ArrayList<FileMetadata>();
    @SerializedName("total_count")
    @Expose
    public Integer totalCount;
    @SerializedName("has_more")
    @Expose
    public Boolean hasMore;
    @SerializedName("cursor")
    @Expose
    public String cursor;

    @Override
    public List<Provider.Metadata> entries() {
        return new ArrayList<Provider.Metadata>(entries);
    }

    @Override
    public String cursor() {
        return cursor;
    }

    @Override
    public boolean hasMore() {
        return hasMore;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
