
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.response;

import com.seagate.alto.provider.lyve.annotation.Generated;

import java.util.ArrayList;
import java.util.List;

@Generated("org.jsonschema2pojo")
public class ListFolderResponce {

    public String parent;
    public String id;
    public List<Entry> entries = new ArrayList<Entry>();
    public Integer totalCount;
    public Boolean hasMore;
    public String cursor;

}
