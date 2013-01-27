/*
 * Copyright 2010-2013, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Map;

@XStreamAlias("ResourceBindingInfo")
public class ResourceBindingInfo {
    private String fromService;
    private String fromResourceId;
    private String toService;
    private String toResourceId;
    private String alias;
    Map<String, String> config;

    public ResourceBindingInfo() {
    }

    public ResourceBindingInfo(String fromService, String fromResourceId, String toService, String         toResourceId, String alias) {
        this.fromService = fromService;
        this.fromResourceId = fromResourceId;
        this.toService = toService;
        this.toResourceId = toResourceId;
        this.alias = alias;
    }

    public String getFromService() {
        return fromService;
    }

    public void setFromService(String fromService) {
        this.fromService = fromService;
    }

    public String getFromResourceId() {
        return fromResourceId;
    }

    public void setFromResourceId(String fromResourceId) {
        this.fromResourceId = fromResourceId;
    }

    public String getToService() {
        return toService;
    }

    public void setToService(String toService) {
        this.toService = toService;
    }

    public String getToResourceId() {
        return toResourceId;
    }

    public void setToResourceId(String toResourceId) {
        this.toResourceId = toResourceId;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Map<String, String> getConfig() {
        return config;
    }

    public void setConfig(Map<String, String> config) {
        this.config = config;
    }
}

