package com.cloudbees.api.oauth;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MediaType;

/**
 *
 * @author Vivek Pandey
 */
public class OauthClientImpl implements OauthClient {
    private final String gcUrl;
    private final Client client = JerseyClientFactory.getClient();

    private static String defaultGcUrl;

    static {
        defaultGcUrl = System.getenv("GRANDCENTRAL_URL");
        if (defaultGcUrl==null)
            defaultGcUrl = "https://grandcentral.cloudbees.com";
    }

    /**
     * OauthClient implementation.
     *
     * @param gcUrl - Grandcentral URI. For example, development: https://grandcentral.beescloud.com,
     *              for production: https://grandcentral.cloudbees.com. Default is production.
     */
    public OauthClientImpl(String gcUrl) {
        if(gcUrl == null){
            this.gcUrl = defaultGcUrl;
        }else{
            this.gcUrl = gcUrl;
        }
    }

    /**
     * Create OAuthClient object that talks to the GrandCentral specified by the <tt>GRANDCENTRAL_URL</tt> environment
     * variable.
     */
    public OauthClientImpl() {
        this(null);
    }

    @Override
    public OauthToken createToken(String email, String password, TokenRequest tokenRequest) throws OauthClientException{
        try{
            WebResource wr = client.resource(gcUrl).path("/api/v2/authorizations");
            wr.addFilter(new HTTPBasicAuthFilter(email, password));

            ClientResponse cr = wr.accept(MediaType.APPLICATION_JSON_TYPE).type(MediaType.APPLICATION_JSON_TYPE).post(ClientResponse.class,tokenRequest);
            AuthorizationResponse resp = cr.getEntity(AuthorizationResponse.class);
            OauthToken token = new OauthToken();
            token.refreshToken = resp.refreshToken;
            token.accessToken = resp.accessToken.token;
            token.account = resp.account;
            token.scopes = resp.accessToken.scopes;
            token.tokenType = resp.accessToken.tokenType;
            token.uid = resp.uid;
            token.email = resp.email;
            return token;
        }catch(Exception e){
            logger.error("Failed to create token", e);
            throw new OauthClientException("Failed to validate token. "+e.getMessage(), e);
        }
    }

    @Override
    public OauthToken validateToken(String clientId, String clientSecret, String token, String... scopes) {
        try{
            WebResource wr = client.resource(gcUrl).path("oauth/tokens").path(token);
            wr.addFilter(new HTTPBasicAuthFilter(clientId, clientSecret));
            ClientResponse cr = wr.get(ClientResponse.class);
            OauthToken oauthToken =  cr.getEntity(OauthToken.class);
            if(oauthToken.expiresIn <= 0){
                return null;
            }
            for(String scope: scopes){
                if(!oauthToken.validateScope(scope)){
                    return null;
                }
            }
            return oauthToken;
        }catch (Exception e){
            logger.error("Failed to get token details",e);
            return null;
        }
    }

    @Override
    public String parseToken(String authenticationHeader) {
        if(authenticationHeader == null){
            logger.debug("Null authorization header");
            return null;
        }

        String[] auth = authenticationHeader.split(" ");
        if(auth.length != 2){
            logger.error("Invalid Authorization header: "+authenticationHeader);
            return null;
        }
        String scheme = auth[0];
        if(!scheme.equals("Bearer")){
            logger.error("Only Bearer authentication scheme is supported. Received scheme: "+scheme);
            return null;
        }

        return Base64.base64Decode(auth[1]);
    }

    @Override
    public String parseToken(HttpServletRequest request) {
        String authenticationHeader = request.getHeader("Authorization");

        if(authenticationHeader == null){
            //Check if there is access_token query parameter
            String tokenParam = request.getParameter("access_token");
            if(tokenParam == null){
                logger.error("Null authorization header");
                return null;
            }
            return tokenParam;
        }

        return parseToken(authenticationHeader);
    }


    private static final Logger logger = LoggerFactory.getLogger(OauthClientImpl.class);
}
