/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ServiceResourceBindResponse")
public class ServiceResourceBindResponse {
    private String message;

    public ServiceResourceBindResponse() {
    }

    public ServiceResourceBindResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}