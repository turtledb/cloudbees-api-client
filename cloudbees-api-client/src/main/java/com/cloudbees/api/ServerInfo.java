/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;
import java.util.Map;

@XStreamAlias("ServerInfo")
public class ServerInfo {
    String id;
    String poolId;
    String state;
    String size;
    List<String> applicationIDs;
    List<String> applicationInstanceIDs;
    Map<String, String> parameters;

    public ServerInfo() {
    }

    public ServerInfo(String id, String state, String poolId) {
        this.state = state;
        this.id = id;
        this.poolId = poolId;
    }

    public String getPoolId() {
        return poolId;
    }

    public void setPoolId(String poolId) {
        this.poolId = poolId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public List<String> getApplicationIDs() {
        return applicationIDs;
    }

    public void setApplicationIDs(List<String> applicationIDs) {
        this.applicationIDs = applicationIDs;
    }

    public Map<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public List<String> getApplicationInstanceIDs() {
        return applicationInstanceIDs;
    }

    public void setApplicationInstanceIDs(List<String> applicationInstanceIDs) {
        this.applicationInstanceIDs = applicationInstanceIDs;
    }
}
