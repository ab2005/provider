/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.lyve.request;

public class Client {

    private String clientId;
    private String clientPlatform;
    private String clientType;
    private String clientVersion;
    private String displayName;

    /**
     *
     * @return
     * The clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     *
     * @param clientId
     * The client_id
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public Client withClientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    /**
     *
     * @return
     * The clientPlatform
     */
    public String getClientPlatform() {
        return clientPlatform;
    }

    /**
     *
     * @param clientPlatform
     * The client_platform
     */
    public void setClientPlatform(String clientPlatform) {
        this.clientPlatform = clientPlatform;
    }

    public Client withClientPlatform(String clientPlatform) {
        this.clientPlatform = clientPlatform;
        return this;
    }

    /**
     *
     * @return
     * The clientType
     */
    public String getClientType() {
        return clientType;
    }

    /**
     *
     * @param clientType
     * The client_type
     */
    public void setClientType(String clientType) {
        this.clientType = clientType;
    }

    public Client withClientType(String clientType) {
        this.clientType = clientType;
        return this;
    }

    /**
     *
     * @return
     * The clientVersion
     */
    public String getClientVersion() {
        return clientVersion;
    }

    /**
     *
     * @param clientVersion
     * The client_version
     */
    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public Client withClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
        return this;
    }

    /**
     *
     * @return
     * The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     *
     * @param displayName
     * The display_name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Client withDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

}
