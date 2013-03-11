/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api.config;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import java.lang.String;import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Fabian Donze
 */
@XStreamAlias("env")
public class Environment extends ParameterHolder {
    @XStreamAsAttribute
    private String name;

    @XStreamImplicit(itemFieldName = "resource")
    private List<ResourceSettings> resources;

    public Environment() {
    }

    public Environment(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ResourceSettings> getResources() {
        if (resources == null)
            resources = new ArrayList<ResourceSettings>();
        return resources;
    }

    public void setResources(List<ResourceSettings> resources) {
        this.resources = resources;
    }
    public void setResource(ResourceSettings resource) {
        deleteResource(resource.getName());
        getResources().add(resource);
    }

    public ResourceSettings getResource(String name) {
        for (ResourceSettings resource : getResources()) {
            if (resource.getName().equals(name))
                return resource;
        }
        return null;
    }
    public void deleteResource(String name) {
        Iterator<ResourceSettings> it = getResources().iterator();
        while (it.hasNext()) {
            if (it.next().getName().equals(name)) {
                it.remove();
            }
        }
    }
}
