/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ServerPoolDeleteResponse")
public class ServerPoolDeleteResponse {
    private boolean deleted;

    public ServerPoolDeleteResponse() {
    }

    public ServerPoolDeleteResponse(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }
}