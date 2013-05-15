/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("ServerPoolListResponse")
public class ServerPoolListResponse {
    private List<ServerPoolInfo> pools;

    public ServerPoolListResponse() {
    }

    public List<ServerPoolInfo> getPools() {
        if(pools == null)
            pools = new ArrayList<ServerPoolInfo>();
        return pools;
    }

    public void setPools(List<ServerPoolInfo> pools) {
        this.pools = pools;
    }
}