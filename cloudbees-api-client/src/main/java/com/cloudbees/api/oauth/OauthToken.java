package com.cloudbees.api.oauth;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 *
 * @author Vivek Pandey
 */
public class OauthToken {
    /**
     * Refresh token lasts for a long time and can be used to obtain additional {@link #accessToken}s.
     */
    @JsonProperty("refresh_token")
    public String refreshToken;

    /**
     * A short-lived opaque token that you'll send in the "Authorize" HTTP header as "Authorize: bearer <i>valueOfAccessToken</i>"
     */
    @JsonProperty("access_token")
    public String accessToken;

    @JsonProperty("token_type")
    public String tokenType;

    /**
     * The number of seconds the access token will be valid, relative to the point of time where
     * the call is issued to obtain this object (such as via {@link OauthClient#validateToken(String, String...)}.
     *
     * 0 or less means the token has already expired.
     */
    @JsonProperty("expires_in")
    public int expiresIn;

    @JsonProperty("client_id")
    public String clientId;

    /**
     * Internal user ID that identifies the user who generated this token.
     */
    @JsonProperty("uid")
    public String uid;

    /**
     * E-mail address of the user identified by {@link #uid}
     */
    @JsonProperty("email")
    public String email;

    /**
     * The account for which this token is valid. OAuth token is always restricted to one account (of
     * all the other accounts that the user might have access to.)
     */
    @JsonProperty("account")
    public String account;

    /**
     * ID that represents this token among other tokens that the user has created.
     * Used for updating/revoking this token.
     */
    @JsonProperty("id")
    public String id;

    /**
     * OAuth scopes of this token.
     *
     * The meaning of the scope values are up to the applications.
     */
    @JsonProperty("scopes")
    public List<String> scopes;


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
