package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthTokenDetail;
import com.cloudbees.api.oauth.OauthClient;
import com.cloudbees.api.oauth.OauthClientException;
import com.cloudbees.api.oauth.OauthToken;
import com.cloudbees.api.oauth.TokenRequest;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public class OauthClientImpl implements OauthClient {
    private final BeesClient bees;
    private final String gcUrl;

    private static final ObjectMapper mapper = createMapper();

    private static ObjectMapper createMapper(){
        ObjectMapper mapper = new ObjectMapper();

        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

    /**
     * OauthClient implementation.
     *
     * @param gcUrl - Grandcentral URI. For example, development: https://grandcentral.beescloud.com,
     *              for production: https://grandcentral.cloudbees.com.
     */
    OauthClientImpl(BeesClient bees, String gcUrl) {
        this.bees = bees;
        this.gcUrl = gcUrl;
    }

    public OauthToken createToken(TokenRequest tokenRequest) throws OauthClientException {
        try{
            OauthTokenDetail resp = bees.jsonPOJORequest(gcUrl + "/api/v2/authorizations", tokenRequest, OauthTokenDetail.class, "POST");

            OauthToken token = new OauthToken();
            token.refreshToken = resp.refreshToken;
            token.accessToken = resp.accessToken.token;
            token.setAccount(resp.account);
            token.scopes = new ArrayList<String>(Arrays.asList(resp.accessToken.scopes));
            token.tokenType = resp.accessToken.tokenType;
            token.uid = resp.uid;
            token.email = resp.email;
            token.expiresIn = resp.expiresIn;
            token.id = resp.id;
            return token;
        }catch(IOException e){
            throw new OauthClientException("Failed to validate token. "+e.getMessage(), e);
        }
    }

    public void deleteToken(String oauthTokenId) throws OauthClientException {
        try {
            bees.jsonPOJORequest(gcUrl + "/api/v2/authorizations/"+oauthTokenId, null, null, "DELETE");
        } catch (IOException e) {
            throw new OauthClientException("Failed to delete OAuth token",e);
        }
    }

    public void deleteToken(OauthToken token) throws OauthClientException {
        deleteToken(token.id);
    }

    public OauthToken validateToken(String token, String... scopes) {
        try{
            OauthToken oauthToken = bees.jsonPOJORequest(gcUrl+"/oauth/tokens/"+token,null,OauthToken.class,"GET");

            if(oauthToken.expiresIn <= 0){
                return null;
            }
            for(String scope: scopes){
                if(!oauthToken.validateScope(scope)){
                    return null;
                }
            }
            return oauthToken;
        }catch (IOException e){
            logger.log(Level.WARNING, "Failed to get token details",e);
            return null;
        }
    }

    public String parseToken(String authenticationHeader) {
        if(authenticationHeader == null){
            logger.fine("Null authorization header");
            return null;
        }

        String[] auth = authenticationHeader.split(" ");
        if(auth.length != 2){
            logger.warning("Invalid Authorization header: " + authenticationHeader);
            return null;
        }
        String scheme = auth[0];
        if(!scheme.equals("Bearer")){
            logger.warning("Only Bearer authentication scheme is supported. Received scheme: " + scheme);
            return null;
        }

        try {
            return new String(Base64.decodeBase64(auth[1].getBytes("US-ASCII")),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }


    private static final Logger logger = Logger.getLogger(OauthClientImpl.class.getName());
}
