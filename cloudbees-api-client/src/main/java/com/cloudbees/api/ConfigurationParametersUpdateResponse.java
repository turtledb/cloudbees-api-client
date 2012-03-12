/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ConfigurationParametersUpdateResponse")
public class ConfigurationParametersUpdateResponse {
    private String status;

    public ConfigurationParametersUpdateResponse() {
    }

    public ConfigurationParametersUpdateResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
