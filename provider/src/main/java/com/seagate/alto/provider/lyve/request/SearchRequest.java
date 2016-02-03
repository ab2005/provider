/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.seagate.alto.provider.lyve.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class SearchRequest {

    @SerializedName("path")
    @Expose
    public String path;
    @SerializedName("query")
    @Expose
    public String query;
    @SerializedName("start")
    @Expose
    public Integer start;
    @SerializedName("max_results")
    @Expose
    public Integer maxResults;
    @SerializedName("mode")
    @Expose
    public String mode;

    public SearchRequest withPath(String path) {
        this.path = path;
        return this;
    }

    public SearchRequest withQuery(String query) {
        this.query = query;
        return this;
    }

    public SearchRequest withStart(int start) {
        this.start = start;
        return this;
    }

    public SearchRequest withMaxResults(int maxResults) {
        this.maxResults = maxResults;
        return this;
    }

    public SearchRequest withMode(String mode) {
        this.mode = mode;
        return this;
    }

}