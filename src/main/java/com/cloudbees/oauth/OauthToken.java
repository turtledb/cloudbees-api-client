package com.cloudbees.oauth;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author Vivek Pandey
 */
public class OauthToken {
    @JsonProperty("refresh_token")
    public String refreshToken;

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

    @JsonProperty("account")
    public String account;

    /**
     * scope -  is space separated list of scopes
     */
    @JsonProperty("scopes")
    public String[] scopes;


    /**
     * Return true if the given scope is fond in the scopes granted with this token
     */
    @JsonIgnore
    public boolean validateScope(String scope){
        if(scope == null){
            return false;
        }
        for(String s: scopes){
            if(s.trim().equals(scope)){
                return true;
            }
        }
        return false;
    }
}
