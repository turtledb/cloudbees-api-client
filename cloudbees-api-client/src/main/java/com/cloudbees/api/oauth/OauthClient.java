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
     * Creates a OAuth token for the current user (used to create {@link BeesClient})
     *
     * @return OauthToken. always non-null if there was error such as invalid credentials
     * @throws OauthClientException if there is any error during token validation
     */
    public OauthToken createToken(TokenRequest tokenRequest) throws OauthClientException;

    /**
     * Deletes a token created by {@link #createToken(TokenRequest)}
     *
     * @see AbstractOauthToken#delete()
     */
    public void deleteToken(String oauthTokenId) throws OauthClientException;

    /**
     * Validates token with the given scopes. Returns null if the given access token is invalid, otherwise OauthToken is returned.
     *
     * <p>
     * {@link BeesClient} must be constructed with OAuth client ID and client secret as the username and password.
     *
     * @param token non-null token
     * @param scopes array of scope that are expected to be granted for this token
     * @return null if the token is invalid such as expired or unknown to the CloudBees OAuth server or the expected
     * scopes are not found.
     */
    public OauthToken validateToken(String token, String... scopes) throws OauthClientException;

    /**
     * Parses Bearer token from HTTP Authorization header
     *
     * @param authorizationHeader HTTP Authorization Header
     *
     * @return Returns null if there is no Bearer token found otherwise a String representing oauth token
     */
    public String parseAuthorizationHeader(String authorizationHeader);
}
