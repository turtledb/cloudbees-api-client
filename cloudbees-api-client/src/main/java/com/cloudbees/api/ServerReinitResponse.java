/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ServerReinitResponse")
public class ServerReinitResponse {
    private String status;
    private String serverId;

    public ServerReinitResponse() {
    }

    public ServerReinitResponse(String status, String serverId) {
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