/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ServiceResourceUnBindResponse")
public class ServiceResourceUnBindResponse {
    private String message;

    public ServiceResourceUnBindResponse() {
    }

    public ServiceResourceUnBindResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}