/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ServerRestoreResponse")
public class ServerRestoreResponse {
    private String status;
    private String serverId;

    public ServerRestoreResponse() {
    }

    public ServerRestoreResponse(String status, String serverId) {
        this.status = status;
        this.serverId = serverId;
    }

    public String getStatus() {
        return status;
    }

    public String getServerId() {
        return serverId;
    }
}