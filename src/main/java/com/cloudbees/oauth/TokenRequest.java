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

    public TokenRequest(String note, String noteUrl, String... scopes) throws OauthClientException {
        this.note = note;
        this.noteUrl = noteUrl;
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

}
