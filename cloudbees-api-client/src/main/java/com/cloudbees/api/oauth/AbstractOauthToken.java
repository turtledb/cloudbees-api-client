package com.cloudbees.api.oauth;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * Common part between {@link OauthToken} and {@link OauthTokenDetail}
 *
 * @author Kohsuke Kawaguchi
 */
public class AbstractOauthToken {
    /**
     * ID that represents this token among other tokens that the user has created.
     * Used for updating/revoking this token.
     */
    @JsonProperty("id")
    public String id;

    /**
     * Refresh token lasts for a long time and can be used to obtain additional {@link #accessToken}s.
     */
    @JsonProperty("refresh_token")
    public String refreshToken;

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
     * The number of seconds the access token will be valid, relative to the point of time where
     * the call is issued to obtain this object (such as via {@link OauthClient#validateToken(String, String...)}.
     *
     * 0 or less means the token has already expired.
     */
    @JsonProperty("expires_in")
    public int expiresIn;

    /**
     * OAuth scopes of this token.
     *
     * The meaning of the scope values are up to the applications.
     */
    @JsonProperty("scopes")
    public List<String> scopes;
}
