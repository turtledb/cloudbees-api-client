package com.cloudbees.api.oauth;

import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Scope definition for {@link OauthClientApplication}.
 *
 * @author Kohsuke Kawaguchi
 * @see OauthClientApplication
 */
public class ScopeDefinition {
    /**
     * Scope name that will show up in the query parameter
     * when you request tokens.
     */
    @JsonProperty
    public String name;

    /**
     * One-liner text description to be shown to users when
     * they are asked to authorize the OAuth token request.
     */
    @JsonProperty
    public String display_name;

//    /**
//     *
//     */
//    @JsonProperty
//    public boolean private_scope;
}
