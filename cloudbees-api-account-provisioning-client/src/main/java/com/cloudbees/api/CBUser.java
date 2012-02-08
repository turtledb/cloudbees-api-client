package com.cloudbees.api;

import java.io.IOException;
import java.util.List;

/**
 * @author Kohsuke Kawaguchi
 */
public class CBUser extends CBObject {
    public String first_name;
    public String last_name;
    public String email;
    /**
     * This is the login ID, a short token without any whitespace, any special characters, etc.
     */
    public String name;
    public String password;

    /**
     * Unique identifier of the user. To be used for most of user management. Once created it can not be modified.
     *
     * (This is not to be set in the request, but available in the response.)
     */
    public String id;

    public List<String> roles;

    public PartiallyCreatedUser partially_created;
    
    public List<CBAccount> accounts;

    /**
     * Creates an user and an account.
     */
    public CBUser createAccount(String name) throws IOException {
        CBAccount acc = new CBAccount();
        acc.name = name;
        return root.postAndRetrieve("/api/users/"+id+"/accounts",acc,CBUser.class,"POST");
    }

    public void delete() throws IOException {
        root.deleteUser(id);
    }

    public CBUser update() throws IOException {
        return root.updateUser(id,this);
    }
}
