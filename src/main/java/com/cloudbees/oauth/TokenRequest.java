package com.cloudbees.oauth;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 *
 * @author Vivek Pandey
 */
public class TokenRequest {
    private final String note;

    private final String noteUrl;

    public TokenRequest(String note, String noteUrl) {
        this.note = note;
        this.noteUrl = noteUrl;
    }

    @JsonProperty("note")
    public String getNote() {
        return note;
    }

    @JsonProperty("note_url")
    public String getNoteUrl() {
        return noteUrl;
    }
}
