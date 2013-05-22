package com.cloudbees.api;

import com.cloudbees.api.oauth.AbstractOauthToken;
import org.codehaus.jackson.annotate.JsonIgnore;

/**
 * Portion of {@link AbstractOauthToken} accessible to {@link OauthClientImpl}.
 *
 * @author Kohsuke Kawaguchi
 */
public class OAuthTokenBase {
    @JsonIgnore
    protected transient OauthClientImpl owner;
}
