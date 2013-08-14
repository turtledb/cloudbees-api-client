package com.cloudbees.api;

import com.cloudbees.api.oauth.OauthClient;
import com.cloudbees.api.oauth.OauthClientApplication;
import com.cloudbees.api.oauth.OauthClientException;
import com.cloudbees.api.oauth.OauthToken;
import com.cloudbees.api.oauth.OauthTokenDetail;
import com.cloudbees.api.oauth.TokenRequest;
import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Collections.*;

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
            return toToken(resp);
        }catch(IOException e){
            throw new OauthClientException("Failed to validate token. "+e.getMessage(), e);
        }
    }

    private OauthToken toToken(OauthTokenDetail resp) {
        OauthToken token = new OauthToken();
        token.owner = this;
        token.refreshToken = resp.refreshToken != null ? resp.refreshToken.token : null;
        token.accessToken = resp.accessToken.token;
        token.setAccount(resp.account);
        token.scope = join(resp.accessToken.scopes,",");
        token.tokenType = resp.accessToken.tokenType;
        token.uid = resp.uid;
        token.email = resp.email;
        token.setExpiresIn(resp.accessToken.expiresIn);
        token.id = resp.id;
        return token;
    }

    private String join(Collection<?> col, String delim) {
        StringBuilder buf = new StringBuilder();
        for (Object o : col) {
            if (buf.length()>0) buf.append(delim);
            buf.append(o);
        }
        return buf.toString();
    }

    public static class OauthTokenDetailList {
        @JsonProperty
        public List<OauthTokenDetail> authorized_tokens;
    }

    public List<OauthTokenDetail> listTokens() throws OauthClientException {
        try{
            OauthTokenDetailList rsp = bees.jsonPOJORequest(gcUrl + "/api/v2/authorizations", null, OauthTokenDetailList.class, "GET");
            for (OauthTokenDetail d : rsp.authorized_tokens) {
                d.owner = this;
            }
            return rsp.authorized_tokens;
        } catch(IOException e) {
            throw new OauthClientException("Failed to list up tokens. "+e.getMessage(), e);
        }
    }

    public void deleteToken(String oauthTokenId) throws OauthClientException {
        try {
            bees.jsonPOJORequest(gcUrl + "/api/v2/authorizations/"+oauthTokenId, null, null, "DELETE");
        } catch (IOException e) {
            throw new OauthClientException("Failed to delete OAuth token",e);
        }
    }

    public OauthToken validateToken(String token, String... scopes) throws OauthClientException {
        OauthToken oauthToken = validateToken(token);
        if (oauthToken==null)   return null;

        if (oauthToken.validateScopes(scopes))
            return oauthToken;
        else
            return null;
    }

    public OauthToken validateToken(String token) throws OauthClientException {
        try{
            OauthToken oauthToken = bees.jsonPOJORequest(gcUrl+"/oauth/tokens/"+token,null,OauthToken.class,"GET");

            if(oauthToken.isExpired()){
                return null;
            }
            return oauthToken;
        }catch (IOException e){
            logger.log(Level.WARNING, "Failed to get token details",e);
            return null;
        }
    }

    public String parseAuthorizationHeader(String authorizationHeader) {
        if(authorizationHeader == null){
            logger.fine("Null authorization header");
            return null;
        }

        String[] auth = authorizationHeader.split(" ");
        if(auth.length != 2){
            logger.warning("Invalid Authorization header: " + authorizationHeader);
            return null;
        }
        String scheme = auth[0];
        if(!scheme.equalsIgnoreCase("Bearer")){
            logger.warning("Only Bearer authentication scheme is supported. Received scheme: " + scheme);
            return null;
        }

        try {
            return new String(Base64.decodeBase64(auth[1].getBytes("US-ASCII")),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    public OauthClientApplication registerApplication(OauthClientApplication input) throws OauthClientException {
        try {
            OauthClientApplication r = bees.jsonPOJORequest(gcUrl + "/api/v2/applications/", input, OauthClientApplication.class, "POST");
            r.owner = this;
            return r;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to register an application", e);
            return null;
        }
    }

    public OauthClientApplication getApplication(String clientId) throws OauthClientException {
        try {
            OauthClientApplication r = bees.jsonPOJORequest(gcUrl + "/api/v2/applications/"+clientId, null, OauthClientApplication.class, "GET");
            r.owner = this;
            return r;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to retrieve an application "+clientId, e);
            return null;
        }
    }

    public void deleteApplication(String clientId) throws OauthClientException {
        try {
            bees.jsonPOJORequest(gcUrl + "/api/v2/applications/"+clientId, null, null, "DELETE");
        } catch (IOException e) {
            throw new OauthClientException("Failed to delete OAuth application",e);
        }
    }

    public static class AppList {
        @JsonProperty
        public List<OauthClientApplication> applications;
    }

    public List<OauthClientApplication> listApplication() throws OauthClientException {
        try {
            AppList r = bees.jsonPOJORequest(gcUrl + "/api/v2/applications/", null, AppList.class, "GET");
            for (OauthClientApplication a : r.applications) {
                a.owner = this;
            }
            return r.applications;
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to list up OAuth applications", e);
            return null;
        }
    }

    public OauthToken exchangeToAccessToken(String authorizationCode, String redirectUri) throws OauthClientException {
        try {
            Map<String,List<String>> params = new HashMap<String, List<String>>();
            params.put("grant_type", singletonList("authorization_code"));
            params.put("code", singletonList(authorizationCode));
            if (redirectUri!=null)
                params.put("redirect_uri", singletonList(redirectUri));

            OauthTokenDetail resp = bees.formUrlEncoded(gcUrl+"/oauth/token", null, params).bind(OauthTokenDetail.class,bees);
            return toToken(resp);
        } catch (IOException e) {
            throw new OauthClientException("Failed to exchange authorization code to access token",e);
        }
    }

    public OauthToken createOAuthClientToken(Collection<String> scopes) throws OauthClientException {
        try {
            Map<String,List<String>> params = new HashMap<String, List<String>>();
            params.put("grant_type", singletonList("client_credentials"));
            params.put("scope", new ArrayList<String>(scopes));

            OauthTokenDetail resp = bees.formUrlEncoded(gcUrl+"/oauth/token", null, params).bind(OauthTokenDetail.class,bees);
            return toToken(resp);
        } catch (IOException e) {
            throw new OauthClientException("Failed to exchange authorization code to access token",e);
        }
    }

    public OauthToken createOAuthClientToken(String... scopes) throws OauthClientException {
        return createOAuthClientToken(Arrays.asList(scopes));
    }

    private static final Logger logger = Logger.getLogger(OauthClientImpl.class.getName());
}
