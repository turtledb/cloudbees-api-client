package com.cloudbees.api.oauth;

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
        this.note = note;
        return this;
    }

    public TokenRequest withNote(String note, String noteUrl) {
        this.note = note;
        this.note = noteUrl;
        return this;
    }

    @JsonProperty("scopes")
    public List<String> getScopes(){
        return Collections.unmodifiableList(scopes);
    }

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

    @JsonProperty("refresh_token")
    public String getRefreshToken() {
        return refreshToken;
    }

    public TokenRequest withRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
        return this;
    }

    @JsonProperty("account_name")
    public String getAccountName() {
        return accountName;
    }

    public TokenRequest withAccountName(String accountName) {
        this.accountName = accountName;
        return this;
    }
}
