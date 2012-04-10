package com.cloudbees.api;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker.Std;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.*;

/**
 * My playground or playing with account provisioning API.
 * TODO: merge this to the base BeesClient.
 *
 * @author Kohsuke Kawaguchi
 */
public class BeesClient2 extends BeesClient {
    private String encodedAccountAuthorization;
    private URL base;

    public BeesClient2(BeesClientConfiguration conf) {
        super(conf);
        init();
    }

    public BeesClient2(String server, String apikey, String secret, String format, String version) {
        super(server, apikey, secret, format, version);
        init();
    }

    private void init() {
        BeesClientConfiguration conf = getBeesClientConfiguration();
        try {
            base = new URL(conf.getServerApiUrl());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid API URL:"+conf.getServerApiUrl(),e);
        }
        if (conf.getApiKey()!=null || conf.getSecret()!=null) {
            String userpassword = conf.getApiKey()+':'+conf.getSecret();
            encodedAccountAuthorization = new String(Base64.encodeBase64(userpassword.getBytes()));
        } else
            encodedAccountAuthorization = null;

    }

    /**
     * Creates an user, including a partial user creation.
     *
     * @see <a href="https://sites.google.com/a/cloudbees.com/account-provisioning-api/home/user-api#TOC-Create-a-User">API spec</a>
     */
    public CBUser createUser(CBUser user) throws IOException {
        return postAndRetrieve("/v2/users",user,CBUser.class, "POST");
    }

    public CBUser updateUser(String id, CBUser user) throws IOException {
        return postAndRetrieve("/v2/users/"+id,user,CBUser.class, "PATCH");
    }

    public void deleteUser(String id) throws IOException {
        postAndRetrieve("/v2/users/" + id, null, null, "DELETE");
    }

    public CBUser addUserToAccount(CBAccount account, CBUser user) throws IOException {
        return postAndRetrieve("/v2/users/"+user.id+"/accounts/"+account.name+"/users",user,CBUser.class, "POST");
    }

    /*package*/ <T> T postAndRetrieve(String apiTail, Object request, Class<T> type, String method) throws IOException {
        URL url = new URL(base,apiTail);
        HttpURLConnection uc = (HttpURLConnection) url.openConnection();
        uc.setRequestProperty("Authorization", "Basic " + encodedAccountAuthorization);

        uc.setDoOutput(true);
        uc.setRequestProperty("Content-type", "application/json");
        uc.setRequestProperty("Accept","application/json");
        uc.setRequestMethod(method);

        if (request!=null)
            MAPPER.writeValue(uc.getOutputStream(), request);
        uc.getOutputStream().close();

        try {
            InputStreamReader r = new InputStreamReader(uc.getInputStream(), "UTF-8");
            String data = IOUtils.toString(r);  // read it upfront to make debugging easier
            if (type==null) {
                return null;
            }
            T ret = MAPPER.readValue(data, type);
            if (ret instanceof CBObject)    // TODO: nested objects?
                ((CBObject)ret).root = this;
            return ret;
        } catch (IOException e) {
            InputStream err = uc.getErrorStream();
            String output="";
            if (err!=null)
                output = "\n"+IOUtils.toString(err);
            throw (IOException)new IOException("Failed to POST to "+url+output).initCause(e);
        }
    }

    public CBAccount getAccount(String name) throws IOException {
        return postAndRetrieve("/v2/accounts/"+name,null,CBAccount.class, "GET");
    }

    public CBUser getUser(String id) throws IOException {
        return postAndRetrieve("/v2/users/"+id,null,CBUser.class, "GET");
    }

    /*package*/ static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setVisibilityChecker(new Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.getDeserializationConfig().set(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
