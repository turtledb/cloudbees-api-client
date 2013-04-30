/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ApplicationConfigUpdateResponse")
public class ApplicationConfigUpdateResponse {
    private String status;

    public ApplicationConfigUpdateResponse() {
    }

    public ApplicationConfigUpdateResponse(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}