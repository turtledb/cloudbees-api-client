package com.cloudbees.api.oauth;

import com.cloudbees.api.OAuthObject;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    /**
     * The mode of token issuance allowed for this application.
     */
    @JsonProperty("grant_type")
    public Set<GrantType> grant_types = new HashSet<GrantType>();

    /**
     * If your user ID belongs to multiple accounts,
     * specify which account this app is registered under.
     */
    @JsonProperty
    public String account;

    @JsonProperty
    public List<ScopeDefinition> scopes=new ArrayList<ScopeDefinition>();

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
