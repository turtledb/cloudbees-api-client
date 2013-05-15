/*
 * Copyright 2010-2011, CloudBees Inc.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ApplicationCreateResponse")
public class ApplicationCreateResponse {
    private ApplicationInfo application;
    private RepositoryInfo repository;
    private CIInfo ci;

    public ApplicationCreateResponse(ApplicationInfo application) {
        this.application = application;
    }

    public ApplicationInfo getApplicationInfo() {
        return application;
    }

    public RepositoryInfo getRepository() {
        return repository;
    }

    public void setRepository(RepositoryInfo repository) {
        this.repository = repository;
    }

    public CIInfo getCIInfo() {
        return ci;
    }

    public void setCIInfo(CIInfo ci) {
        this.ci = ci;
    }
}
