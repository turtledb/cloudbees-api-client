package com.cloudbees.oauth;

import org.codehaus.jackson.annotate.JsonProperty;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Vivek Pandey
 */
public class TokenRequest {
    private final String note;

    private final String noteUrl;

    private final List<String> scopes = new ArrayList<String>();

    private final String accessToken;

    /**
     *
     * Create a Json serializable Oauth token request.
     *
     * @param note optional. Tells something about the oauth application
     * @param noteUrl optional. Your application URL
     * @param accessToken optional. Access token that should be used to generate a new token. If present it must have the token granting scope grant:
     *                    https://api.cloudbees.com/v2/users/user/generate_token scope.
     * @param scopes Optional. array of scopes to be granted with this token. The default scope is  https://api.cloudbees.com/v2/users/user,
     *               which is user read and write scope. If you are creating token to crete other tokens with specific scopes you must ask for
     *               https://api.cloudbees.com/v2/users/user/generate_token scope scope.
     *
     * @throws OauthClientException In case there is an error
     */
    public TokenRequest(String note, String noteUrl, String accessToken, String... scopes) throws OauthClientException {
        this.note = note;
        this.noteUrl = noteUrl;
        this.accessToken = accessToken;
        for(String scope: scopes){
            try {
                new URI(scope);
            } catch (URISyntaxException e) {
                throw new OauthClientException("Scope must be a valid URI", e);
            }
        }
    }

    @JsonProperty("note")
    public String getNote() {
        return note;
    }

    @JsonProperty("note_url")
    public String getNoteUrl() {
        return noteUrl;
    }

    @JsonProperty("scopes")
    public List<String> getScopes(){
        return Collections.unmodifiableList(scopes);
    }

    @JsonProperty("access_token")
    public String getAccessToken() {
        return accessToken;
    }
}
