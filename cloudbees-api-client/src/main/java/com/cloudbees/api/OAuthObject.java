package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthClient;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Remote object created by {@link OauthClient}, accessible to {@link OauthClientImpl}.
 *
 * @author Kohsuke Kawaguchi
 */
public class OAuthObject {
    @JsonIgnore
    protected transient OauthClientImpl owner;
}
