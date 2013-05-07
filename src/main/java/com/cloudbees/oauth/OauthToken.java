package com.cloudbees.oauth;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author Vivek Pandey
 */
public class OauthToken {
    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("expires_in")
    public int expiresIn;

    @JsonProperty("client_id")
    public String clientId;

    @JsonProperty("uid")
    public String uid;

    @JsonProperty("email")
    public String email;

    @JsonProperty("accounts")
    public String[] accounts;

    /**
     * scope -  is space separated list of scopes
     */
    @JsonProperty("scope")
    public String scope;

    @JsonIgnore
    public String[] getScopes(){
        if(scope == null){
            return new String[0];
        }

        return scope.split(" ");
    }

    /**
     * Return true if the given scope is fond in the scopes granted with this token
     */
    @JsonIgnore
    public boolean validateScope(String scope){
        if(scope == null){
            return false;
        }
        for(String s: getScopes()){
            if(s.trim().equals(scope)){
                return true;
            }
        }
        return false;
    }
}
