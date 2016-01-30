
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.response;

import java.util.ArrayList;
import java.util.List;

public class AccountResponse {

    public String accountId;
    public String created;
    public String lastModified;
    public String email;
    public Name name;
    public List<Client> clients = new ArrayList<Client>();
    public List<Device> devices = new ArrayList<Device>();
    public Boolean isInternal;
    public State state;
    public Integer tosVersion;
    public Integer spaceQuota;

}
