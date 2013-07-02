package com.cloudbees.api.oauth;

import com.cloudbees.api.BeesClientConfiguration;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parameters for {@link OauthClient#createToken(TokenRequest)} call.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class TokenRequest {
    private String accountName;

    private String note;

    private String noteUrl;

    private final List<String> scopes = new ArrayList<String>();

    private String refreshToken;

    /* accessType tells whether client intends to access protected resources as a one time or shorter duration access
       or for longer duration, for example when user is not present at the browser offline accessType will result in a
       refresh_token that can be used by the client to generate new access_tokens.
     */
    private String accessType = "online";

    /**
     *
     * Create a Json serializable Oauth token request.
     *
     * @param note optional. Tells something about the oauth application
     * @param noteUrl optional. Your application URL
     * @param refreshToken optional. Refresh token that should be used to generate a new token.
     * @param accountName required. Name of the account
     * @param scopes Optional. array of scopes to be granted with this token. The default scope is  https://api.cloudbees.com/v2/users/user,
     *               which is user read and write scope. If you are creating token to crete other tokens with specific scopes you must ask for
     *               https://api.cloudbees.com/v2/users/user/generate_token scope scope.
     *
     * @throws IllegalArgumentException In case there is an error
     */
    public TokenRequest(String note, String noteUrl, String refreshToken, String accountName, String... scopes) {
        this.note = note;
        this.noteUrl = noteUrl;
        this.accountName = accountName;
        this.refreshToken = refreshToken;
        for(String scope: scopes)
            withScope(scope);
    }

    public TokenRequest() {}

    @JsonProperty("note")
    public String getNote() {
        return note;
    }

    @JsonProperty("note_url")
    public String getNoteUrl() {
        return noteUrl;
    }

    public TokenRequest withNote(String note) {
        return withNote(note,null);
    }

    /**
     * @param note
     *      One liner human readable text describing who requested this token. This will
     *      aid users in reviewing and revoking tokens later. Can be null.
     * @param noteUrl
     *      URL that provides more details about the note. Can be null.
     */
    public TokenRequest withNote(String note, String noteUrl) {
        this.note = note;
        this.note = noteUrl;
        return this;
    }

    @JsonProperty("scopes")
    public List<String> getScopes(){
        return Collections.unmodifiableList(scopes);
    }

    /**
     * Adds more scopes to this request. A scope needs to look like an URI.
     */
    public TokenRequest withScope(String scope) {
        if (scope!=null) {
            try {
                new URI(scope);
                this.scopes.add(scope);
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Scope must be a valid URI: "+scope, e);
            }
        }
        return this;
    }

    public TokenRequest withScopes(String... scopes) {
        for (String scope : scopes) {
            withScope(scope);
        }
        return this;
    }

    @JsonProperty("refresh_token")
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * Instead of authenticating the {@linkplain OauthClient#createToken(TokenRequest) create token request}
     * with the parameters you set in {@link BeesClientConfiguration} (such as API key + secret, or username + password),
     * authenticate the call with the specified refresh token generated earlier.
     */
    public TokenRequest withRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    @JsonProperty("account_name")
    public String getAccountName() {
        return accountName;
    }

    /**
     * Sets the account name that the generated token is restricted to.
     *
     * An OAuth token is always restricted to just one account (out of all the accounts that the user has access to.)
     */
    public TokenRequest withAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }

    @JsonProperty("access_type")
    public String getAccessType() {
        return accessType;
    }

    /**
     * Request that the refresh token be generated aside from access token.
     *
     * Unlike access token which will expire automatically in a short amount of time (currently an hour),
     * a refresh token allows the client to generate more access tokens in the future.
     *
     * @see #withRefreshToken(String)
     */
    public TokenRequest withGenerateRequestToken(boolean b) {
        this.accessType = b ? "offline" : "online";
        return this;
    }
}
