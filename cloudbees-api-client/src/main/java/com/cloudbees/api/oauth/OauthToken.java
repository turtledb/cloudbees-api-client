package com.cloudbees.api.oauth;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Vivek Pandey
 */
public class OauthToken extends AbstractOauthToken {

    /**
     * A short-lived opaque token that you'll send in the "Authorize" HTTP header as "Authorize: bearer <i>valueOfAccessToken</i>"
     */
    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("client_id")
    public String clientId;

    /**
     * Right now an OAuth token only grants access to one account only, but this might be something
     * we may want to change later, so we aren't providing direct access to this property.
     */
    @JsonProperty("account")
    private String account;

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

    /**
     * Checks if this token grants access to the specified account.
     */
    public boolean belongsToAccount(String account) {
        return this.account.equals(account);
    }

    /**
     * List up all the accounts to which this token grants some access.
     */
    public Collection<String> listAccounts() {
        return Collections.singleton(account);
    }

    public void setAccount(String account) {
        this.account = account;
    }
}
