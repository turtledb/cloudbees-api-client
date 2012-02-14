package com.cloudbees.api;

import java.io.IOException;
import java.util.List;

/**
 * Represetns an user in the CloudBees platform.
 *
 * <p>
 * A user typically represents a human that can login to the system. One user can belong
 * to multiple accounts, and vice versa.
 *
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

    /**
     * This field is set by the server when you {@linkplain BeesClient2#createUser(CBUser) create an user}
     * with incomplete information, to indicate that the user was partially created and the rest of the registration
     * needs to be completed by the user before the account becomes fully usable.
     */
    public PartiallyCreatedUser partially_created;
    
    public List<CBAccount> accounts;

    /**
     * Creates an account and add this user to it.
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
