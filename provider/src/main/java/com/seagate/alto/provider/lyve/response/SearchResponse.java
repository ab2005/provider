
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.response;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.seagate.alto.provider.Provider;

import java.util.ArrayList;
import java.util.List;

public class SearchResponse implements Provider.SearchResult{

    @SerializedName("matches")
    @Expose
    public List<Match> matches = new ArrayList<Match>();
    @SerializedName("more")
    @Expose
    public Boolean more;
    @SerializedName("start")
    @Expose
    public Long start;
    @SerializedName("num_found")
    @Expose
    public Integer numFound;

    @Override
    public ArrayList<Provider.Metadata> matches() {
        ArrayList<Provider.Metadata> list = new ArrayList<>();
        if (matches != null) {
            for(Match item : matches) {
                list.add(item.metadata);
            }
        }
        return list;
    }

    @Override
    public boolean hasMore() {
        return more;
    }

    @Override
    public long start() {
        return start;
    }
}
