package com.cloudbees.api.cr;

import com.cloudbees.api.oauth.OauthToken;
import org.codehaus.jackson.Base64Variants;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;

/**
 * Provides credential for outbound requests to {@link CloudResource}s.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Credential {
    /**
     * Decorates the connection by adding necessary information for authentication,
     * most typically the "Authorization" header.
     *
     * @throws IOException
     *      If an error happens and the request should not proceed.
     */
    public abstract void authorizeRequest(HttpURLConnection con) throws IOException;

    /**
     * Creates a {@link Credential} implementation that always put the specified OAuth token.
     */
    public static Credential oauth(String token) {
        try {
            final String authorization = "Bearer "+ Base64Variants.MIME_NO_LINEFEEDS.encode(token.getBytes("UTF-8"));
            return new Credential() {
                @Override
                public void authorizeRequest(HttpURLConnection con) {
                    con.setRequestProperty("Authorization",authorization);
                }
            };
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Creates a {@link Credential} implementation that always put the specified OAuth token.
     */
    public static Credential oauth(OauthToken token) {
        return oauth(token.accessToken);
    }
}
