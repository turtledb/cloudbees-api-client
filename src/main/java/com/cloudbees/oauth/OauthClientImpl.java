package com.cloudbees.oauth;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.Base64;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Vivek Pandey
 */
public class OauthClientImpl implements OauthClient {
    private final String gcUrl;
    private final Client client = JerseyClientFactory.getClient();
    private final ObjectMapper om = JacksonConfigurator.getMapper();

    private static final String defaultGcUrl = "https://grandcentral.cloudbees.com";

    public OauthClientImpl(String gcUrl) {
        if(gcUrl == null){
            this.gcUrl = defaultGcUrl;
        }else{
            this.gcUrl = gcUrl;
        }

    }

    public OauthClientImpl() {
        this(null);
    }

    @Override
    public OauthToken createToken(String email, String password, TokenRequest tokenRequest) {
        try{
            WebResource wr = client.resource(gcUrl).path("/api/v2/authorizations");
            wr.addFilter(new HTTPBasicAuthFilter(email, password));

            ClientResponse cr = wr.post(ClientResponse.class,tokenRequest);
            AuthorizationResponse resp = cr.getEntity(AuthorizationResponse.class);
            OauthToken token = new OauthToken();
            token.accessToken = resp.accessToken.token;
            token.accounts = resp.accounts;
            token.scope = resp.scope;
            token.tokenType = resp.tokenType;
            token.uid = resp.uid;
            token.email = resp.email;
            return token;
        }catch(Exception e){
            logger.error("Failed to create token", e);
            return null;
        }
    }

    @Override
    public OauthToken validateToken(String clientId, String clientSecret, String token) {
        try{
            WebResource wr = client.resource(gcUrl).path("oauth/tokens").path(token);
            wr.addFilter(new HTTPBasicAuthFilter(clientId, clientSecret));
            ClientResponse cr = wr.get(ClientResponse.class);
            return cr.getEntity(OauthToken.class);
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


    private static final Logger logger = LoggerFactory.getLogger(OauthClientImpl.class);
}
