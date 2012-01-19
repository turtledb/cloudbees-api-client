/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ApplicationScaleResponse")
public class ApplicationScaleResponse {
    private String status;

    public ApplicationScaleResponse() {
    }

    public ApplicationScaleResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}