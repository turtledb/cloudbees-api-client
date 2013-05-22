package com.cloudbees.api.oauth;

import javax.servlet.http.HttpServletRequest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Additional utility for {@link OauthClient} for servlet applications.
 *
 * This class is separated from {@link OauthClient} so  that it can be used
 * without the servlet API.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class OauthClientServletUtil {
    /**
     * Parses Bearer token from HTTP Authentication header or from access_token query or form parameter
     *
     * @param request HttpServletRequest
     *
     * @return Returns null if there is no Bearer token found otherwise a String representing oauth token
     */
    public static String parseToken(HttpServletRequest request, OauthClient client) {
        String authenticationHeader = request.getHeader("Authorization");

        if(authenticationHeader == null){
            //Check if there is access_token query parameter
            String tokenParam = request.getParameter("access_token");
            if(tokenParam == null){
                LOGGER.log(Level.FINE, "Null authorization header");
                return null;
            }
            return tokenParam;
        }

        return client.parseAuthorizationHeader(authenticationHeader);
    }

    private static final Logger LOGGER = Logger.getLogger(OauthClientServletUtil.class.getName());
}
