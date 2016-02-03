/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.request;

public class CreateAccountRequest {
    private String email;
    private String password;
    private Name name;
    private Client client;
    private Boolean isInternal;

    /**
     *
     * @return
     * The email
     */
    public String getEmail() {
        return email;
    }

    /**
     *
     * @param email
     * The email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    public CreateAccountRequest withEmail(String email) {
        this.email = email;
        return this;
    }

    /**
     *
     * @return
     * The password
     */
    public String getPassword() {
        return password;
    }

    /**
     *
     * @param password
     * The password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public CreateAccountRequest withPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     *
     * @return
     * The name
     */
    public Name getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(Name name) {
        this.name = name;
    }

    public CreateAccountRequest withName(Name name) {
        this.name = name;
        return this;
    }

    /**
     *
     * @return
     * The client
     */
    public Client getClient() {
        return client;
    }

    /**
     *
     * @param client
     * The client
     */
    public void setClient(Client client) {
        this.client = client;
    }

    public CreateAccountRequest withClient(Client client) {
        this.client = client;
        return this;
    }

    /**
     *
     * @return
     * The isInternal
     */
    public Boolean getIsInternal() {
        return isInternal;
    }

    /**
     *
     * @param isInternal
     * The is_internal
     */
    public void setIsInternal(Boolean isInternal) {
        this.isInternal = isInternal;
    }

    public CreateAccountRequest withIsInternal(Boolean isInternal) {
        this.isInternal = isInternal;
        return this;
    }

}
