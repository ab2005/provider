/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.request;

import com.seagate.alto.provider.lyve.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ListFolderRequest {

    public String path;
    public Integer limit;
    public Boolean includeDeleted;
    public Boolean includeMediaInfo;
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