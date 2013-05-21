package com.cloudbees.api.oauth;

import com.cloudbees.api.BeesClient;

/**
 * Defines OAuth-related CloudBees API.
 *
 * Also see {@link OauthClientServletUtil} that defines related convenience methods for webapps.
 *
 * @author Vivek Pandey
 * @see BeesClient#getOauthClient()
 */
public interface OauthClient {

    /**
     * Creates a OAuth token using user's username and password (or API key and secret.)
     *
     * @return OauthToken. always non-null if there was error such as invalid credentials
     * @throws OauthClientException if there is any error during token validation
     */
    public OauthToken createToken(String username, String password, TokenRequest tokenRequest) throws OauthClientException;

    /**
     * Validates token with the given scopes. Returns null if the given access token is invalid, otherwise OauthToken is returned.
     *
     * @param clientId OAuth client_id
     * @param clientSecret OAuth client_secret
     * @param token non-null token
     * @param scopes array of scope that are expected to be granted for this token
     * @return null if the token is invalid such as expired or unknown to the CloudBees OAuth server or the expected
     * scopes are not found.
     */
    public OauthToken validateToken(String clientId, String clientSecret, String token, String... scopes) throws OauthClientException;

    /**
     * Parses Bearer token from HTTP Authentication header
     *
     * @param authenticationHeader HTTP Authentication Header
     *
     * @return Returns null if there is no Bearer token found otherwise a String representing oauth token
     */
    public String parseToken(String authenticationHeader);
}
