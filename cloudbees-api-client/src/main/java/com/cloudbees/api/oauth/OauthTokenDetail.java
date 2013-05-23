package com.cloudbees.api.oauth;

import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Date;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class OauthTokenDetail extends AbstractOauthToken {
    @JsonProperty("access_token")
    public AccessTokenDetail accessToken;

    @JsonProperty("refresh_token")
    public TokenDetail refreshToken;

    @JsonProperty("account")
    public String account;

    @JsonProperty("app")
    public App app;

    public static class TokenDetail {
        @JsonProperty("token")
        public String token;

        @JsonProperty("created_at")
        public Date createdAt;

        @JsonProperty("updated_at")
        public Date updatedAt;

        @JsonProperty("scopes")
        public List<String> scopes;
    }

    public static class AccessTokenDetail extends TokenDetail {
        @JsonProperty("expires_in")
        public int expiresIn;

        @JsonProperty("token_type")
        public String tokenType;
    }

    public static class App {
        @JsonProperty("name")
        public String name;

        @JsonProperty("url")
        public String url;
    }
}
