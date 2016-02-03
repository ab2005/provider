
/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.request;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("email")
    @Expose
    public String email;
    @SerializedName("password")
    @Expose
    public String password;
    @SerializedName("client")
    @Expose
    public Client client;

    public LoginRequest withEmail(String email) {
        this.email = email;
        return this;
    }

    public LoginRequest withPassword(String password) {
        this.password = password;
        return this;
    }

    public LoginRequest withClient(Client client) {
        this.client = client;
        return this;
    }

}
