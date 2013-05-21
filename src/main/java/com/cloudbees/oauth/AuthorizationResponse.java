package com.cloudbees.oauth;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;

/**
 * @author Vivek Pandey
 */
class AuthorizationResponse {
    @JsonProperty("id")
    public String id;

    @JsonProperty("access_token")
    public AccessToken accessToken;

    @JsonProperty("scope")
    public String scope;

    @JsonProperty("token_type")
    public String tokenType;

    @JsonProperty("uid")
    public String uid;

    @JsonProperty("email")
    public String email;


    @JsonProperty("created_at")
    public Date createdAt;

    @JsonProperty("updated_at")
    public Date updatedAt;

    @JsonProperty("accounts")
    public String[] accounts;

    @JsonProperty("app")
    public App app;

    public static class AccessToken{
        @JsonProperty("token")
        public String token;

        @JsonProperty("created_at")
        public Date createdAt;
    }


    public static class App{
        @JsonProperty("name")
        public String name;

        @JsonProperty("url")
        public String url;
    }
}
