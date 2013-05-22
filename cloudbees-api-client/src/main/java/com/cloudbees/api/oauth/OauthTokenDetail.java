package com.cloudbees.api.oauth;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class OauthTokenDetail extends AbstractOauthToken {
    @JsonProperty("access_token")
    public AccessToken accessToken;

    @JsonProperty("account")
    public String account;

    @JsonProperty("created_at")
    public Date createdAt;

    @JsonProperty("updated_at")
    public Date updatedAt;

    @JsonProperty("app")
    public App app;

    public static class AccessToken {
        @JsonProperty("token")
        public String token;

        @JsonProperty("token_type")
        public String tokenType;

        @JsonProperty("created_at")
        public Date createdAt;

        @JsonProperty("expires_in")
        public int expiresIn;

        @JsonProperty("scopes")
        public List<String> scopes;

    }


    public static class App {
        @JsonProperty("name")
        public String name;

        @JsonProperty("url")
        public String url;
    }
}
