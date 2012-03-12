/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ConfigurationParametersDeleteResponse")
public class ConfigurationParametersDeleteResponse {
    private String status;

    public ConfigurationParametersDeleteResponse() {
    }

    public ConfigurationParametersDeleteResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
