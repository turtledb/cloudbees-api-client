/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ServiceResourceDeleteResponse")
public class ServiceResourceDeleteResponse {
    private boolean deleted;

    public ServiceResourceDeleteResponse() {
    }

    public ServiceResourceDeleteResponse(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }
}