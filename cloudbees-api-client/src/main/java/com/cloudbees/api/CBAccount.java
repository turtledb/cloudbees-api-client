package com.cloudbees.api;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Represents an account in the CloudBees platform.m
 * <p/>
 * <p/>
 * An account is a unit of billing.
 *
 * @author Kohsuke Kawaguchi
 */
public class CBAccount extends CBObject {

    /**
     * This is the login ID of the account, a short alpha-numeric token without any special characters.
     */
    String name;

    public List<CBSubscription> subscriptions;

    public CBUser addUser(CBUser user) throws IOException {
        return root.addUserToAccount(this, user);
    }

    /**
     * Adds the new subscription to the user.
     */
    // this is what I want
    public CBSubscription addSubscription(CBSubscription sub) throws IOException {
        return root.jsonPOJORequest("v2/accounts/" + name + "/subscriptions",
                sub, CBSubscription.class, "POST");
    }

    /**
     * @deprecated Use {@link #addSubscription(CBSubscription)} when it's ready
     */
    // this is what we have today
    public CBSubscription addSubscription(String userid, CBSubscription sub) throws IOException {
        // TODO: why do we need to specify the user here?
        return root.jsonPOJORequest("v2/users/" + userid + "/accounts/" + name + "/subscriptions",
                sub, CBSubscription.class, "POST");
    }

    public List<CBSubscription> getSubscriptions() throws IOException {
        return Arrays.asList(root.jsonPOJORequest("v2/accounts/" + name + "/subscriptions", null,
                CBSubscription[].class, "GET"));
    }
}
