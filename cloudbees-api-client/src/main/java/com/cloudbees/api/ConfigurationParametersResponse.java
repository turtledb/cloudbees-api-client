/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ConfigurationParametersResponse")
public class ConfigurationParametersResponse {
    private String configuration;

    public ConfigurationParametersResponse() {
    }

    public ConfigurationParametersResponse(String configuration) {
        this.configuration = configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public String getConfiguration() {
        return configuration;
    }
}
