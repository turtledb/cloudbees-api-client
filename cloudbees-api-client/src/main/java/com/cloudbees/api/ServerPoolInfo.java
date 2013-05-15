/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.List;
import java.util.Map;

@XStreamAlias("ServerPoolInfo")
public class ServerPoolInfo {
    String id;
    String name;
    String state;
    Map<String, String> settings;
    List<ServerInfo> servers;
    List<ApplicationInfo> applications;

    public ServerPoolInfo() {
    }

    public ServerPoolInfo(String id, String name, Map<String, String> settings) {
        this.id = id;
        this.settings = settings;
        this.name = name;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<ServerInfo> getServers() {
        return servers;
    }

    public void setServers(List<ServerInfo> servers) {
        this.servers = servers;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public List<ApplicationInfo> getApplications() {
        return applications;
    }

    public void setApplications(List<ApplicationInfo> applications) {
        this.applications = applications;
    }
}
