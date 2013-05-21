package com.cloudbees.api;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;

/**
 * @author Vivek Pandey
 */
class AuthorizationResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("refresh_token")
    public String refreshToken;


    @JsonProperty("access_token")
    public AccessToken accessToken;

    @JsonProperty("uid")
    public String uid;

    @JsonProperty("email")
    public String email;

    @JsonProperty("account")
    public String account;


    @JsonProperty("created_at")
    public Date createdAt;

    @JsonProperty("updated_at")
    public Date updatedAt;

    @JsonProperty("scopes")
    public String[] scopes;

    @JsonProperty("app")
    public App app;

    public static class AccessToken{
        @JsonProperty("token")
        public String token;

        @JsonProperty("token_type")
        public String tokenType;

        @JsonProperty("created_at")
        public Date createdAt;

        @JsonProperty("expires_in")
        public int expiresIn;

        @JsonProperty("scopes")
        public String[] scopes;

    }


    public static class App{
        @JsonProperty("name")
        public String name;

        @JsonProperty("url")
        public String url;
    }
}
