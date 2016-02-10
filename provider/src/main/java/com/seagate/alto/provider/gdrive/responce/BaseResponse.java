/*
 * Copyright (c) 2015. Seagate Technology PLC. All rights reserved.
 */

package com.seagate.alto.provider.gdrive.responce;

public abstract class BaseResponse {

    private String error = null;

    public String getError() {
        return error;
    }
}
