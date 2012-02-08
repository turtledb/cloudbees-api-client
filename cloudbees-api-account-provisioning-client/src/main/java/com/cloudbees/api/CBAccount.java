package com.cloudbees.api;

import java.io.IOException;
import java.util.List;

/**
 * Represents an account in the CloudBees platform.m
 *
 * <p>
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
        return root.addUserToAccount(this,user);
    }

    /**
     * Adds the new subscription to the user.
     */
    public CBSubscription addSubscription(String userId, CBSubscription sub) throws IOException {
        // TODO: why do we need to specify the user here?
        return root.postAndRetrieve("/api/users/"+userId+"/accounts/"+name+"/subscriptions",
                sub, CBSubscription.class, "POST");
    }
}
