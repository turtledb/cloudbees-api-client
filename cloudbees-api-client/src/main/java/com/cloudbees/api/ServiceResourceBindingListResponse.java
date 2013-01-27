/*
 * Copyright 2010-2013, CloudBees Inc.
 */
package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("ServiceResourceBindingListResponse")
public class ServiceResourceBindingListResponse {
    private List<ResourceBindingInfo> bindings;

    public ServiceResourceBindingListResponse() {
    }

    public List<ResourceBindingInfo> getBindings() {
        if(bindings == null)
            bindings = new ArrayList<ResourceBindingInfo>();
        return bindings;
    }

    public void setBindings(List<ResourceBindingInfo> bindings) {
        this.bindings = bindings;
    }
}