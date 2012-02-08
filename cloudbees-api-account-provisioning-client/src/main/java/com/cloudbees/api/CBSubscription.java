package com.cloudbees.api;

import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Kohsuke Kawaguchi
 */
public class CBSubscription extends CBObject {
    public String service;
    public String plan;

    public ObjectNode config;
}
