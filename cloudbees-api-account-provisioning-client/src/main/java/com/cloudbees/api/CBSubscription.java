package com.cloudbees.api;

import org.codehaus.jackson.node.ObjectNode;

/**
 * @author Kohsuke Kawaguchi
 */
public class CBSubscription extends CBObject {
    public String service;
    public String plan;

    /**
     * This is used from client->server to tweak the details of the subscrpition.
     */
    public ObjectNode settings;
    /**
     * This is used from server->client to represent the current details of the subscription.
     */
    public ObjectNode config;
}
