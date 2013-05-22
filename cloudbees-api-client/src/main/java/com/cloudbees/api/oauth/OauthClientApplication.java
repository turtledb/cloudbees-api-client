package com.cloudbees.api.oauth;

import com.cloudbees.api.OAuthObject;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.List;

/**
 * State representation of OAuth client application.
 *
 * @author Kohsuke Kawaguchi
 */
public class OauthClientApplication extends OAuthObject {
    @JsonProperty
    public String name;

    @JsonProperty
    public String callback_uri;

    @JsonProperty
    public String app_url;

    // see https://github.com/cloudbees/grandcentral/pull/1
//    @JsonProperty
//    public String grant_type = "client_credentials";

    /**
     * If your user ID belongs to multiple accounts,
     * specify which account this app is registered under.
     */
    @JsonProperty
    public String account;

    @JsonProperty
    public List<ScopeDefinition> scopes;

    /**
     * Client ID of the application.
     *
     * This value is assigned by {@link OauthClient#registerApplication(OauthClientApplication)} when you make a call.
     */
    @JsonProperty
    public String client_id;

    /**
     * Client secret that forms a pair with {@link #client_id}
     *
     * This value is assigned by {@link OauthClient#registerApplication(OauthClientApplication)} when you make a call.
     */
    @JsonProperty
    public String client_secret;

    /**
     * Unregisters this application and voids its client ID/secret.
     */
    public void delete() throws OauthClientException {
        owner.deleteApplication(client_id);
    }
}
