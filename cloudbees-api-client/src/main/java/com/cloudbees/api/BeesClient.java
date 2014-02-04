/*
 * Copyright 2010-2012, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.api;

import com.cloudbees.api.config.ConfigParameters;
import com.cloudbees.api.config.ParameterSettings;
import com.cloudbees.api.config.ResourceSettings;
import com.cloudbees.api.oauth.OauthClient;
import com.cloudbees.upload.ArchiveUtils;
import com.cloudbees.upload.JarUtils;
import com.cloudbees.utils.AppConfigHelper;
import com.cloudbees.utils.ZipHelper;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker.Std;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.ANY;
import static org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.NONE;

/**
 * Main entry point to the CloudBees API.
 *
 * @author Fabian Donze
 * @author Kohsuke Kawaguchi
 */
public class BeesClient extends BeesClientBase {
    /**
     * The encoded value we send in as the "Authorization" header.
     */
    private String encodedAccountAuthorization;

    /**
     * The API endpoint, such as "https://api.cloudbees.com/"
     */
    private URL base;

    static Logger logger = Logger.getLogger(BeesClient.class.getSimpleName());

    /**
     * Used for mapping JSON to Java objects.
     */
    /*package*/ static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.setVisibilityChecker(new Std(NONE, NONE, NONE, NONE, ANY));
        MAPPER.getDeserializationConfig().set(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }


    public BeesClient(BeesClientConfiguration beesClientConfiguration) {
        super(beesClientConfiguration);
        init();
    }

    /**
     * Creates a client that talks to the CloudBees production API endpoint
     *
     * To obtain your API key an the secret, visit https://grandcentral.cloudbees.com/user/keys
     *
     * If you have reigstered an OAuth application and talking to CloudBees as that OAuth
     * application (for example to validate OAuth tokens you received from your users via browser),
     * then you can specify that OAuth client ID and the secret.
     */
    public BeesClient(String apikey, String secret) {
        this("https://api.cloudbees.com/api ", apikey, secret);
    }

    public BeesClient(String server, String apikey, String secret) {
        this(server, apikey, secret,null,null);
    }

    public BeesClient(String server, String apikey, String secret,
                      String format, String version) {
        // TODO: this encodePassword is considered harmful as it creates assymetry between two constructors
        super(server, apikey, encodePassword(secret, version), format,
                version);
        init();
    }

    /**
     * Common initialization code in this class.
     */
    private void init() {
        BeesClientConfiguration conf = getBeesClientConfiguration();
        try {
            base = new URL(conf.getServerApiUrl());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid API URL:" + conf.getServerApiUrl(), e);
        }
        encodedAccountAuthorization = conf.getAuthorization();
    }

    /**
     * Obtains another interface of the API client that defines OAuth APIs.
     *
     * In API world, api-staging is not in a separate environment,
     * it is part of the main production environment but offers the latest
     * and greatest interface to the production data model.
     *
     * URL Resolution:
     * api.cloudbees.com         => grandcentral.cloudbees.com
     * api-staging.cloudbees.com => grandcentral.cloudbees.com
     * api.beescloud.com         => grandcentral.beescloud.com
     * api-staging.beescloud.com => grandcentral.beescloud.com
     *
     * <p>
     * This method does not involve any remote call.
     *
     * @return never null
     */
    public OauthClient getOauthClient() {
        String gc = base.toExternalForm();
        gc = gc.replace("//api.",         "//grandcentral.");
        gc = gc.replace("//api-staging.", "//grandcentral.");

        if (gc.equals(base.toExternalForm())) {
            throw new RuntimeException("Unable to determine GrandCentral URL from " + base.toExternalForm());
        }

        // cut off the path portion
        while (true) {
            int scheme = gc.indexOf("://");
            int path = gc.lastIndexOf('/');
            if (path>scheme+3) {
                gc = gc.substring(0,path);
            } else
                break;
        }
    
        return new OauthClientImpl(this, gc);
    }

    /**
     * Create OAuth client using the given GC URL. It's useful for client apps who want to test their functionality with
     * GC instances running locally or elsewhere for individual testing.
     *
     * It also decouples RUN api instance from GC instance, useful in certain test scenarios
     *
     * @param gcUrl If null defaults to https://grandcentral.cloudbees.com
     *
     */
    public OauthClient getOauthClient(String gcUrl) {
        if(gcUrl == null){
            gcUrl = "https://grandcentral.cloudbees.com";
        }
        return new OauthClientImpl(this, gcUrl);
    }

    /**
     * Creates an user, including a partial user creation.
     *
     * @see <a href="https://sites.google.com/a/cloudbees
     *      .com/account-provisioning-api/home/user-api#TOC-Create-a-User">API spec</a>
     */
    public CBUser createUser(CBUser user) throws IOException {
        return jsonPOJORequest("v2/users", user, CBUser.class, "POST");
    }

    /**
     * Updates the user record.
     *
     * @param id   The ID of the user to update. Corresponds to {@link CBUser#id}.
     * @param user You should only set fields that you want to update, and leave everything else to null, to indicate
     *             those values should remain untouched.
     */
    public CBUser updateUser(String id, CBUser user) throws IOException {
        return jsonPOJORequest("v2/users/" + id, user, CBUser.class, "PATCH");
    }

    /**
     * Deletes an user.
     */
    public void deleteUser(String id) throws IOException {
        jsonPOJORequest("v2/users/" + id, null, null, "DELETE");
    }

    public CBUser addUserToAccount(CBAccount account, CBUser user) throws IOException {
        return jsonPOJORequest("v2/users/" + user.id + "/accounts/" + account.name + "/users", user, CBUser.class,
                "POST");
    }

    /**
     * The actual engine behind the REST API call.
     * <p/>
     * It sends a request in JSON and expects a JSON response back.
     * Note that for historical reasons, there's the other half of the API that uses query parameters + digital signing.
     *
     * @param apiTail The end point to hit. Appended to {@link #base}. Shouldn't start with '/'
     * @param request JSON-bound POJO object that represents the request payload, or null if none.
     * @param type    JSON-bound POJO class to unmarshal the response into.
     * @param method  HTTP method name like GET or POST.
     * @throws IOException If the communication fails.
     */
    protected <T> T jsonPOJORequest(String apiTail, Object request, Class<T> type, String method) throws IOException {
        String content = null;
        if (request != null) {
            content = MAPPER.writeValueAsString(request);
        }
        HttpReply resp = jsonRequest(apiTail, method, null, content);
        return resp.bind(type,this);
    }

    /**
     * Sends a request in JSON and expects a JSON response back.
     *
     * @param urlTail The end point to hit. Appended to {@link #base}. Shouldn't start with '/'
     * @param method  HTTP method name like GET or POST.
     * @param headers
     *@param jsonContent  The json request payload, or null if none.  @throws IOException If the communication fails.
     */
    public HttpReply jsonRequest(String urlTail, String method, Map<String, String> headers, String jsonContent) throws IOException {
        HttpMethodBase httpMethod;

        String urlString = absolutize(urlTail);

        trace("API call: " + urlString);
        if (method.equalsIgnoreCase("GET")) {
            httpMethod = new GetMethod(urlString);
        } else if ((method.equalsIgnoreCase("POST"))) {
            httpMethod = new PostMethod(urlString);
        } else if ((method.equalsIgnoreCase("PUT"))) {
            httpMethod = new PutMethod(urlString);
        } else if ((method.equalsIgnoreCase("DELETE"))) {
            httpMethod = new DeleteMethod(urlString);
        } else if ((method.equalsIgnoreCase("PATCH"))) {
            httpMethod = new PatchMethod(urlString);
        } else if ((method.equalsIgnoreCase("HEAD"))) {
            httpMethod = new HeadMethod(urlString);
        } else if ((method.equalsIgnoreCase("TRACE"))) {
            httpMethod = new TraceMethod(urlString);
        } else if ((method.equalsIgnoreCase("OPTIONS"))) {
            httpMethod = new OptionsMethod(urlString);
        } else
            throw new IOException("Method not supported: " + method);

        httpMethod.setRequestHeader("Accept", "application/json");
        if (jsonContent != null && httpMethod instanceof EntityEnclosingMethod) {
            StringRequestEntity requestEntity = new StringRequestEntity(jsonContent, "application/json", "UTF-8");
            ((EntityEnclosingMethod)httpMethod).setRequestEntity(requestEntity);
            trace("Payload: " + jsonContent);
        }

        return executeRequest(httpMethod,headers);
    }

    /**
     * Sends a request in JSON and expects a JSON response back.
     *
     * @param urlTail The end point to hit. Appended to {@link #base}. Shouldn't start with '/'
     * @param headers HTTP headers
     * @param params
     *      Form parameters
     */
    /*package*/ HttpReply formUrlEncoded(String urlTail, Map<String, String> headers, Map<String,List<String>> params) throws IOException {
        String urlString = absolutize(urlTail);
        trace("API call: " + urlString);
        PostMethod httpMethod = new PostMethod(urlString);

        httpMethod.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");

        for (Entry<String, List<String>> e : params.entrySet()) {
            for (String v : e.getValue()) {
                httpMethod.addParameter(e.getKey(),v);
            }
        }

        return executeRequest(httpMethod,headers);
    }

    /**
     * Computes the absolute URL to send the request to from the tail portion.
     */
    private String absolutize(String urlTail) throws IOException {
        URL url = new URL(base, urlTail);
        String urlString;
        try {
            urlString = url.toURI().toString();
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid API URL:" + url.toString(), e);
        }
        return urlString;
    }

    /**
     * Executes an HTTP method.
     */
    private HttpReply executeRequest(HttpMethod httpMethod, Map<String, String> headers) throws IOException {
        BeesClientConfiguration conf = getBeesClientConfiguration();
        HttpClient httpClient = HttpClientHelper.createClient(conf);

        if (encodedAccountAuthorization != null)
            httpMethod.setRequestHeader("Authorization", encodedAccountAuthorization);

        if (headers != null) {
            for (Map.Entry<String, String> entry: headers.entrySet()) {
                httpMethod.setRequestHeader(entry.getKey(), entry.getValue());
            }
        }

        int status = 500;
        String rsp = "Error";
        try {
            status = httpClient.executeMethod(httpMethod);
            rsp = IOUtils.toString(httpMethod.getResponseBodyAsStream());
        } catch (IOException e) {
            throw (IOException)new IOException("Failed to " + httpMethod.getName() + " : " + httpMethod.getURI() + " : code=" + status + " response=" + e.getMessage()).initCause(e);
        } finally {
            httpMethod.releaseConnection();
        }

        trace(status + ": " + rsp);

        return new HttpReply(httpMethod, status, rsp);
    }

    public CBAccount getAccount(String name) throws IOException {
        return jsonPOJORequest("v2/accounts/" + name, null, CBAccount.class, "GET");
    }

    /**
     * Looks up the user by ID.
     */
    public CBUser getUser(String id) throws IOException {
        return jsonPOJORequest("v2/users/" + id, null, CBUser.class, "GET");
    }

    /**
     * Retrieves the information of the current user.
     *
     * The "current user" refers to the user who generated the token used to authenticate the method call.
     */
    public CBUser getSelfUser() throws IOException {
        return getUser("self");
    }

    /**
     * Looks up the user by the public key fingerprint
     *
     * @param sshPublicKeyFingerprint Fingerprint formatted as "12:34:56:..:aa:bb:cc" (case insensitive)
     */
    public CBUser getUserByFingerprint(String sshPublicKeyFingerprint) throws IOException {
        return jsonPOJORequest("v2/users/fingerprint/" + sshPublicKeyFingerprint, null, CBUser.class, "GET");
    }

    public SayHelloResponse sayHello(String message) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("message", message);
        String url = getRequestURL("say.hello", params);
        String response = executeRequest(url);
        return (SayHelloResponse) readResponse(response);
    }

    public ApplicationGetSourceUrlResponse applicationGetSourceUrl(
            String appId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.getSourceUrl", params);
        String response = executeRequest(url);
        return (ApplicationGetSourceUrlResponse) readResponse(response);
    }

    public ApplicationDeleteResponse applicationDelete(String appId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.delete", params);
        String response = executeRequest(url);
        return (ApplicationDeleteResponse) readResponse(response);
    }

    public ApplicationRestartResponse applicationRestart(String appId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.restart", params);
        String response = executeRequest(url);
        return (ApplicationRestartResponse) readResponse(response);
    }

    public ApplicationStatusResponse applicationStart(String appId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.start", params);
        String response = executeRequest(url);
        return (ApplicationStatusResponse) readResponse(response);
    }

    public ApplicationStatusResponse applicationStop(String appId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.stop", params);
        String response = executeRequest(url);
        return (ApplicationStatusResponse) readResponse(response);
    }

    public ApplicationStatusResponse applicationHibernate(String appId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.hibernate", params);
        String response = executeRequest(url);
        ApplicationStatusResponse apiResponse =
            (ApplicationStatusResponse)readResponse(response);
        return apiResponse;
    }

    /**
     * Returns all the applications in all the account sthat you belong to.
     * <p/>
     * Short-hand for {@code applicationList(null)}.
     */
    public ApplicationListResponse applicationList() throws Exception {
        return applicationList(null);
    }

    /**
     * Returns all the applications in the specified account.
     *
     * @param account if null, returns all the applications from all the accounts that you belong to.
     * @since 1.1.3
     */
    public ApplicationListResponse applicationList(String account) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        if (account != null) {
            params.put("account", account);
        }
        String url = getRequestURL("application.list", params);
        String response = executeRequest(url);
        return (ApplicationListResponse) readResponse(response);
    }

    public ApplicationInfo applicationInfo(String appId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.info", params);
        String response = executeRequest(url);
        ApplicationInfoResponse apiResponse =
                (ApplicationInfoResponse) readResponse(response);
        return apiResponse.getApplicationInfo();
    }

    public ApplicationSetMetaResponse applicationSetMeta(String appId,
                                                         Map<String, String> metaAttrs) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.putAll(metaAttrs);
        params.put("app_id", appId);
        String url = getRequestURL("application.setMeta", params);
        String response = executeRequest(url);
        return (ApplicationSetMetaResponse) readResponse(response);
    }

    /**
     * @deprecated
     */
    public ApplicationJarHashesResponse applicationJarCrcs(String appId, Map<String, String> hashes) throws Exception {
        return applicationJarHashes(appId, hashes);
    }

    public ApplicationJarHashesResponse applicationJarHashes(String appId, Map<String, String> hashes)
            throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        params.put("hashes", createParameter(hashes));

        String url = getApiUrl("application.jarHashes").toString();
        params.put("action", "application.jarHashes");
        // use the upload method (POST) to handle the potentially large "hashes" parameter payload
        String response = executeUpload(url, params, new HashMap<String, File>(), null);
        return (ApplicationJarHashesResponse) readResponse(response);
    }

    /**
     * @deprecated use {@link #applicationDeployEar(String, String, String, java.io.File, java.io.File, UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployEar(
            String appId, String environment, String description, String earFile,
            String srcFile, UploadProgress progress) throws Exception {
        return applicationDeployEar(appId, environment, description,
                asFile(earFile), asFile(srcFile), progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployEar(
            String appId, String environment, String description, File earFile,
            File srcFile, UploadProgress progress) throws Exception {
        String archiveType = "ear";
        return applicationDeployArchive(appId, environment, description,
                earFile, srcFile, archiveType, false, progress);
    }

    /**
     * @deprecated use {@link #applicationDeployWar(String, String, String, File, File, UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployWar(
            String appId, String environment, String description, String warFile,
            String srcFile, UploadProgress progress) throws Exception {
        return applicationDeployWar(appId, environment, description, asFile(warFile),
                asFile(srcFile), progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployWar(
            String appId, String environment, String description, File warFile,
            File srcFile, UploadProgress progress) throws Exception {
        return applicationDeployWar(appId, environment, description, warFile,
                srcFile, true, progress);
    }

    /**
     * @deprecated use {@link #applicationDeployWar(String, String, String, File, File, boolean, UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployWar(
            String appId, String environment, String description, String warFile,
            String srcFile, boolean deltaDeploy, UploadProgress progress) throws Exception {
        return applicationDeployWar(appId, environment, description,
                asFile(warFile), asFile(srcFile), deltaDeploy, progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployWar(
            String appId, String environment, String description, File warFile,
            File srcFile, boolean deltaDeploy, UploadProgress progress) throws Exception {
        String archiveType = "war";
        return applicationDeployArchive(appId, environment, description,
                warFile, srcFile, archiveType, deltaDeploy, progress);
    }

    /**
     * @deprecated use {@link #applicationDeployArchive(String, String, String, File, File, String, UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, String earFile,
            String srcFile, String archiveType, UploadProgress progress) throws Exception {
        return applicationDeployArchive(appId, environment, description, asFile(earFile), asFile(srcFile), archiveType,
                progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, File earFile,
            File srcFile, String archiveType, UploadProgress progress) throws Exception {
        return applicationDeployArchive(appId, environment, description, earFile, srcFile, archiveType, false,
                progress);
    }

    /**
     * @deprecated use {@link #applicationDeployArchive(String, String, String, File, File, String, boolean,
     *             UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, String earFile,
            String srcFile, String archiveType, boolean deltaDeploy, UploadProgress progress) throws Exception {
        return applicationDeployArchive(appId, environment, description, asFile(earFile), asFile(srcFile), archiveType,
                deltaDeploy, progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, File earFile,
            File srcFile, String archiveType, boolean deltaDeploy, UploadProgress progress) throws Exception {
        return applicationDeployArchive(appId, environment, description, earFile, srcFile, archiveType, deltaDeploy,
                null, progress);
    }

    /**
     * @deprecated use {@link #applicationDeployArchive(String, String, String, File, File, String, boolean, Map,
     *             UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, String earFile,
            String srcFile, String archiveType, boolean deltaDeploy, Map<String, String> parameters,
            UploadProgress progress) throws Exception {
        return applicationDeployArchive(appId, environment, description, asFile(earFile), asFile(srcFile), archiveType,
                deltaDeploy, parameters, progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, File earFile,
            File srcFile, String archiveType, boolean deltaDeploy, Map<String, String> parameters,
            UploadProgress progress) throws Exception {
        return applicationDeployArchive(new ApplicationDeployArgs.Builder(appId)
                .environment(environment).description(description)
                .deployPackage(earFile, archiveType).srcFile(srcFile)
                .incrementalDeployment(deltaDeploy).withParams(parameters)
                .withProgressFeedback(progress).build());
    }

    public ApplicationDeployArchiveResponse applicationDeployArchive(ApplicationDeployArgs args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, File> fileParams = new HashMap<String, File>();
        params.put("app_id", args.appId);

        File archiveFile = args.archiveFile;

        // Currently do not support ear file for delta upload
        boolean deployDelta = false;
        boolean deployJarDelta = false;
        // Create delta deploy File
        if (args.deltaDeploy && !args.archiveType.equals("ear")) {
            trace("Get existing checksums");
            ApplicationCheckSumsResponse applicationCheckSumsResponse = applicationCheckSums(args.appId, false);
            if (logger.isLoggable(Level.FINER)) {
                for (Map.Entry<String, Long> entry : applicationCheckSumsResponse.getCheckSums().entrySet()) {
                    logger.finer("Entry: " + entry.getKey() + " CRC: " + entry.getValue());
                }
            }
            if (applicationCheckSumsResponse.getCheckSums().size() == 0) {
                trace("No existing checksums, upload full archive");
            } else {
                trace("Creating Delta archive for: " + archiveFile);
                archiveFile = ArchiveUtils.createDeltaWarFile(applicationCheckSumsResponse.getCheckSums(), archiveFile,
                        archiveFile.getParent());
                deployDelta = true;
                if (applicationCheckSumsResponse.getSnapshotID() != null)
                    params.put("delta_snapshot_id", applicationCheckSumsResponse.getSnapshotID());
            }
        }

        if (args.deltaDeploy && !args.archiveType.equals("ear")) {
            trace("Get existing jar hashes");
            ApplicationJarHashesResponse applicationJarHashesResponse =
                    applicationJarHashes(args.appId, JarUtils.getJarHashes(archiveFile));
            if (applicationJarHashesResponse.getJarHash().size() == 0) {
                trace("No existing jars");
            } else {
                trace("Creating Delta2 archive for: " + archiveFile);
                File archiveFile2 = JarUtils.createDeltaWarFile(applicationJarHashesResponse.getJarHash(), archiveFile,
                        archiveFile.getParent());
                // Delete the old delta archive
                if (deployDelta) {
                    archiveFile.delete();
                }
                archiveFile = archiveFile2;
                deployJarDelta = true;
            }
        }

        if (deployDelta || deployJarDelta) {
            trace("Uploading delta archive: " + archiveFile);
        }

        File archiveFileSrc = args.srcFile;
        long uploadSize = archiveFile.length();
        if (archiveFileSrc != null) {
            uploadSize += archiveFileSrc.length();
        }

        fileParams.put("archive", archiveFile);
        params.put("archive_type", args.archiveType);

        params.put("create", Boolean.valueOf(args.create).toString());

        if (args.environment != null) {
            params.put("environment", args.environment);
        }

        if (args.description != null) {
            params.put("description", args.description);
        }

        if (archiveFileSrc != null) {
            fileParams.put("src", archiveFileSrc);
        }

        params.put("parameters", createParameter(args.parameters));
        params.put("variables", createParameter(args.variables));

        // extend the deploy invocation timeout to 4 hours
        long expireTime = System.currentTimeMillis() + 4 * 60 * 60 * 1000;
        params.put("expires", Long.toString(expireTime / 1000));

        String url = getApiUrl("application.deployArchive").toString();
        params.put("action", "application.deployArchive");
        String response = executeUpload(url, params, fileParams, args.progress);
        try {
            return (ApplicationDeployArchiveResponse) readResponse(response);
        } finally {
            // Delete the delta archive file
            if (deployDelta || deployJarDelta) {
                archiveFile.delete();
            }
        }
    }

    public ApplicationCheckSumsResponse applicationCheckSums(String appId) throws Exception {
        return applicationCheckSums(appId, true);
    }

    public ApplicationCheckSumsResponse applicationCheckSums(String appId, boolean traceResponse) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.checkSums", params);
        String response = executeRequest(url);
        return (ApplicationCheckSumsResponse) readResponse(response);
    }

    public ApplicationScaleResponse applicationScale(String appId, int unit) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("unit", "" + unit);
        params.put("app_id", appId);
        String url = getRequestURL("application.scale", params);
        String response = executeRequest(url);
        return (ApplicationScaleResponse) readResponse(response);
    }
    
    public DatabaseCreateResponse databaseCreate(String domain, String dbId,
                                                 String username, String password) throws Exception {
        return databaseCreate(domain, dbId, username, password, null);
    }
    public DatabaseCreateResponse databaseCreate(String domain, String dbId,
                String username, String password, String plan) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        if (username != null)
            params.put("database_username", username);
        if (password != null)
            params.put("database_password", password);
        params.put("domain", domain);
        if (plan != null)
            params.put("database_plan", plan);
        String url = getRequestURL("database.create", params);
        String response = executeRequest(url);
        return (DatabaseCreateResponse) readResponse(response);
    }

    public DatabaseDeleteResponse databaseDelete(String dbId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        String url = getRequestURL("database.delete", params);
        String response = executeRequest(url);
        return (DatabaseDeleteResponse) readResponse(response);
    }

    public DatabaseInfo databaseInfo(String dbId, boolean fetchPassword) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        params.put("fetch_password", ((Boolean) fetchPassword).toString());
        String url = getRequestURL("database.info", params);
        String response = executeRequest(url);
        DatabaseInfoResponse apiResponse =
                (DatabaseInfoResponse) readResponse(response);
        return apiResponse.getDatabaseInfo();
    }

    /**
     * Returns all the databases in all the account sthat you belong to.
     * <p/>
     * Short-hand for {@code databaseList(null)}.
     */
    public DatabaseListResponse databaseList() throws Exception {
        return databaseList(null);
    }

    /**
     * Returns all the databases in the specified account.
     *
     * @param account if null, returns all the databases from all the accounts that you belong to.
     * @since 1.1.3
     */
    public DatabaseListResponse databaseList(String account) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        if (account != null) {
            params.put("account", account);
        }
        String url = getRequestURL("database.list", params);
        String response = executeRequest(url);
        return (DatabaseListResponse) readResponse(response);
    }

    /**
     * @deprecated use databaseUpdate
     * @param dbId    The database ID
     * @param password  The database password
     * @return   DatabaseSetPasswordResponse
     * @throws Exception
     */
    public DatabaseSetPasswordResponse databaseSetPassword(String dbId, String password) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        params.put("database_password", password);
        String url = getRequestURL("database.setPassword", params);
        String response = executeRequest(url);
        return (DatabaseSetPasswordResponse) readResponse(response);
    }

    public DatabaseInfo databaseUpdate(String dbID, Map<String, String> parameters) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbID);
        params.put("parameters", createParameter(parameters));
        String url = getRequestURL("database.update", params);
        String response = executeRequest(url);
        return (DatabaseInfo)readResponse(response);
    }

    public DatabaseSnapshotListResponse databaseSnapshotList(String dbId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        String url = getRequestURL("database.snapshot.list", params);
        String response = executeRequest(url);
        return (DatabaseSnapshotListResponse) readResponse(response);
    }

    public DatabaseSnapshotDeleteResponse databaseSnapshotDelete(String dbId, String snapshotId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        params.put("snapshot_id", snapshotId);
        String url = getRequestURL("database.snapshot.delete", params);
        String response = executeRequest(url);
        return (DatabaseSnapshotDeleteResponse) readResponse(response);
    }

    public DatabaseSnapshotDeployResponse databaseSnapshotDeploy(String dbId, String snapshotId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        params.put("snapshot_id", snapshotId);
        String url = getRequestURL("database.snapshot.deploy", params);
        String response = executeRequest(url);
        return (DatabaseSnapshotDeployResponse) readResponse(response);
    }

    public DatabaseSnapshotInfo databaseSnapshotCreate(String dbId, String snapshotTitle) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        if (snapshotTitle != null) {
            params.put("snapshot_title", snapshotTitle);
        }
        String url = getRequestURL("database.snapshot.create", params);
        String response = executeRequest(url);
        return (DatabaseSnapshotInfo) readResponse(response);
    }

    public DatabaseBackupListResponse databaseBackupList(String dbId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        String url = getRequestURL("database.backup.list", params);
        String response = executeRequest(url);
        return (DatabaseBackupListResponse) readResponse(response);
    }

    public DatabaseBackupResponse databaseBackupCreate(String dbId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        String url = getRequestURL("database.backup.create", params);
        String response = executeRequest(url);
        return (DatabaseBackupResponse) readResponse(response);
    }

    public DatabaseBackupResponse databaseBackupRestore(String dbId, String backupId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        params.put("backup_id", backupId);
        String url = getRequestURL("database.backup.restore", params);
        String response = executeRequest(url);
        return (DatabaseBackupResponse) readResponse(response);
    }

    public DatabaseBackupResponse databaseBackupDelete(String dbId, String backupId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        params.put("backup_id", backupId);
        String url = getRequestURL("database.backup.delete", params);
        String response = executeRequest(url);
        return (DatabaseBackupResponse) readResponse(response);
    }

    public DatabaseClusterListResponse databaseClusterList(String account) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account);
        String url = getRequestURL("database.cluster.list", params);
        String response = executeRequest(url);
        return (DatabaseClusterListResponse) readResponse(response);
    }

    public AccountKeysResponse accountKeys(String domain, String user, String password) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user", user);
        params.put("password", password);
        if (domain != null) {
            params.put("domain", domain);
        }
        String url = getRequestURL("account.keys", params);
        String response = executeRequest(url);
        return (AccountKeysResponse) readResponse(response);
    }

    public AccountListResponse accountList() throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        String url = getRequestURL("account.list", params);
        String response = executeRequest(url);
        return (AccountListResponse) readResponse(response);
    }

    public ApplicationConfiguration getApplicationConfiguration(String warFilePath, String account,
                                                                String[] environments) throws Exception {
        ApplicationConfiguration appConfig;
        File deployFile = asFile(warFilePath);
        if (deployFile.exists()) {
            appConfig = getAppConfig(deployFile, environments, new String[]{"deploy"});
        } else {
            throw new IllegalArgumentException("File not found: " + warFilePath);
        }

        String appid = appConfig.getApplicationId();
        if (appid == null || appid.equals("")) {
            throw new IllegalArgumentException("No application id specified");
        }

        String[] appIdParts = appid.split("/");
        if (appIdParts.length < 2) {
            if (account != null && !account.equals("")) {
                appConfig.setApplicationId(account + "/" + appid);
            } else {
                throw new IllegalArgumentException("Application account not specified");
            }
        }
        return appConfig;
    }

    public ConfigurationParametersUpdateResponse configurationParametersUpdate(String resourceId, String configType,
                                                                               File resourceFile) throws Exception {
        assertNotNull(resourceId,"resourceId");
        assertNotNull(configType,"configType");
        assertNotNull(resourceFile,"resourceFile");

        Map<String, String> params = new HashMap<String, String>();
        Map<String, File> fileParams = new HashMap<String, File>();

        params.put("resource_id", resourceId);
        params.put("config_type", configType);
        fileParams.put("resources", resourceFile);

        String url = getApiUrl("configuration.parameters.update").toString();
        params.put("action", "configuration.parameters.update");
        // use the upload method (POST) to handle the potentially large resource list
        String response = executeUpload(url, params, fileParams, null);
        return (ConfigurationParametersUpdateResponse) readResponse(response);
    }

    /**
     * Updates the configuration of the given resource from the configuration object model.
     */
    public ConfigurationParametersUpdateResponse configurationParametersUpdate(
            String resourceId, String configType, ConfigParameters model) throws Exception {

        File xmlFile = File.createTempFile("conf", "xml");
        FileWriter fos = null;
        try {
            fos = new FileWriter(xmlFile);
            fos.write(model.toXML());
            fos.close();
            return configurationParametersUpdate(resourceId,configType,xmlFile);
        } finally {
            IOUtils.closeQuietly(fos);
            xmlFile.delete();
        }
    }

    public ConfigurationParametersDeleteResponse configurationParametersDelete(String resourceId, String configType)
            throws Exception {
        assertNotNull(resourceId,"resourceId");
        assertNotNull(configType,"configType");

        Map<String, String> params = new HashMap<String, String>();
        params.put("resource_id", resourceId);
        params.put("config_type", configType);
        String url = getRequestURL("configuration.parameters.delete", params);
        String response = executeRequest(url);
        return (ConfigurationParametersDeleteResponse) readResponse(response);
    }

    public ConfigurationParametersResponse configurationParameters(String resourceId, String configType)
            throws Exception {
        assertNotNull(resourceId,"resourceId");
        assertNotNull(configType,"configType");

        Map<String, String> params = new HashMap<String, String>();
        params.put("resource_id", resourceId);
        params.put("config_type", configType);
        String url = getRequestURL("configuration.parameters", params);
        String response = executeRequest(url);
        return (ConfigurationParametersResponse) readResponse(response);
    }

    /**
     * Perform {@link #configurationParameters(String, String)} and obtains the result as a data-bound object.
     */
    public ConfigParameters configurationParametersAsObject(String resourceId, String configType) throws Exception {
        return ConfigParameters.parse(configurationParameters(resourceId,configType).getConfiguration());
    }

    public ServiceListResponse serviceList(String account) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account);
        String url = getRequestURL("service.list", params);
        String response = executeRequest(url);
        return (ServiceListResponse) readResponse(response);
    }

    public ServiceSubscriptionInfo serviceSubscribe(String service, String plan, String account,
                                                    Map<String, String> settings) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account);
        params.put("service", service);
        if (plan != null) {
            params.put("plan", plan);
        }
        params.put("settings", createParameter(settings));
        String url = getRequestURL("service.subscribe", params);
        String response = executeRequest(url);
        return ((ServiceSubscriptionResponse) readResponse(response)).getSubscription();
    }

    public ServiceSubscriptionDeleteResponse serviceUnSubscribe(String service, String subscriptionId)
            throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("subscription_id", subscriptionId);
        params.put("service", service);
        String url = getRequestURL("service.unsubscribe", params);
        String response = executeRequest(url);
        return (ServiceSubscriptionDeleteResponse) readResponse(response);
    }

    public ServiceSubscriptionInfo serviceSubscriptionUpdate(String service, String plan, String subscriptionId,
                                                             Map<String, String> settings) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("subscription_id", subscriptionId);
        params.put("service", service);
        if (plan != null) {
            params.put("plan", plan);
        }
        params.put("settings", createParameter(settings));
        String url = getRequestURL("service.subscription.update", params);
        String response = executeRequest(url);
        return ((ServiceSubscriptionResponse) readResponse(response)).getSubscription();
    }

    public ServiceSubscriptionInfo serviceSubscriptionInfo(String service, String subscriptionId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("subscription_id", subscriptionId);
        params.put("service", service);
        String url = getRequestURL("service.subscription.info", params);
        String response = executeRequest(url);
        return ((ServiceSubscriptionResponse) readResponse(response)).getSubscription();
    }

    public ServiceSubscriptionListResponse serviceSubscriptionList(String account) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account);
        String url = getRequestURL("service.subscription.list", params);
        String response = executeRequest(url);
        return (ServiceSubscriptionListResponse) readResponse(response);
    }

    public ServiceResourceInfo serviceResourceInfo(String service, String resourceId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("service", service);
        params.put("resource_id", resourceId);
        String url = getRequestURL("service.resource.info", params);
        String response = executeRequest(url);
        return ((ServiceResourceResponse) readResponse(response)).getResource();
    }

    public ServiceResourceListResponse serviceResourceList(String service, String account, String resourceType)
            throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account);
        params.put("service", service);
        if (resourceType != null) {
            params.put("resource_type", resourceType);
        }
        String url = getRequestURL("service.resource.list", params);
        String response = executeRequest(url);
        return (ServiceResourceListResponse) readResponse(response);
    }
    
    public ServiceResourceBindingListResponse resourceBindingList(String service, String    resourceId) throws Exception
    {
        return resourceBindingList(service, resourceId, false);
    }
    public ServiceResourceBindingListResponse resourceBindingList(String service, String    resourceId, boolean bidirectional) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("resource_id", resourceId);
        params.put("service", service);
        params.put("bidirectional", Boolean.toString(bidirectional));
        String url = getRequestURL("resource.binding.list", params);
        String response = executeRequest(url);
        ServiceResourceBindingListResponse apiResponse =
            (ServiceResourceBindingListResponse)readResponse(response);
        return apiResponse;
    }

    public AccountRegionListResponse accountRegionList(String account, String service) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account);
        if (service != null)
            params.put("service", service);
        String url = getRequestURL("account.region.list", params);
        String response = executeRequest(url);
        return (AccountRegionListResponse)readResponse(response);
    }

    public ApplicationInstanceListResponse applicationInstanceList(String appId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.instance.list", params);
        String response = executeRequest(url);
        ApplicationInstanceListResponse apiResponse =
                (ApplicationInstanceListResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationInstanceStatusResponse applicationInstanceReplace(String instanceId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);
        String url = getRequestURL("application.instance.replace", params);
        String response = executeRequest(url);
        ApplicationInstanceStatusResponse apiResponse =
                (ApplicationInstanceStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationInstanceStatusResponse applicationInstanceRestart(String instanceId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);
        String url = getRequestURL("application.instance.restart", params);
        String response = executeRequest(url);
        ApplicationInstanceStatusResponse apiResponse =
                (ApplicationInstanceStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationInstanceStatusResponse applicationInstanceDelete(String instanceId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);
        String url = getRequestURL("application.instance.delete", params);
        String response = executeRequest(url);
        ApplicationInstanceStatusResponse apiResponse =
                (ApplicationInstanceStatusResponse)readResponse(response);
        return apiResponse;
    }

    public com.cloudbees.api.ApplicationInstanceInfo applicationInstanceTagsUpdate(String instanceId, Map<String, String> tags, boolean replace) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);
        params.put("reset", Boolean.toString(replace));
        params.put("parameters", createParameter(tags));

        String url = getApiUrl("application.instance.update").toString();
        params.put("action", "application.instance.update");
        // use the upload method (POST) to handle the potentially large tags payload
        String response = executeUpload(url, params, new HashMap<String, File>(), null);
        com.cloudbees.api.ApplicationInstanceInfo apiResponse =
                (com.cloudbees.api.ApplicationInstanceInfo)readResponse(response);
        return apiResponse;
    }

    public com.cloudbees.api.ApplicationInstanceInfo applicationInstanceInfo(String instanceId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);

        String url = getRequestURL("application.instance.info", params);
        String response = executeRequest(url);
        com.cloudbees.api.ApplicationInstanceInfo apiResponse =
                (com.cloudbees.api.ApplicationInstanceInfo)readResponse(response);
        return apiResponse;
    }

    public ApplicationInstanceInvokeResponse applicationInstanceInvoke(String instanceId, String invoke, Map<String, String>parameters) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);
        params.put("invoke",invoke);
        params.put("parameters", createParameter(parameters));

        String url = getRequestURL("application.instance.invoke", params);
        String response = executeRequest(url);
        ApplicationInstanceInvokeResponse apiResponse =
                (ApplicationInstanceInvokeResponse)readResponse(response);
        return apiResponse;
    }

    public void applicationInstanceTailLog(String instanceId, String logName, OutputStream out) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("instance_id", instanceId);
        params.put("log_name", logName);
        String url = getRequestURL("tail", params, false);
        trace("API call: " + url);
        InputStream input = executeCometRequest(url);

        byte[] bytes = new byte[1024];
        int numRead = input.read(bytes);
        while (numRead != -1) {
            out.write(bytes, 0, numRead);
            numRead = input.read(bytes);
        }
    }

    public ApplicationSnapshotListResponse applicationSnapshotList(String appId) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.snapshot.list", params);
        String response = executeRequest(url);
        return (ApplicationSnapshotListResponse) readResponse(response);
    }

    public ApplicationSnapshotStatusResponse applicationSnapshotDelete(String snapshotId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("snapshot_id", snapshotId);
        String url = getRequestURL("application.snapshot.delete", params);
        String response = executeRequest(url);
        ApplicationSnapshotStatusResponse apiResponse =
                (ApplicationSnapshotStatusResponse)readResponse(response);
        return apiResponse;
    }

    public com.cloudbees.api.ApplicationSnapshotInfo applicationSnapshotInfo(String snapshotId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("snapshot_id", snapshotId);
        String url = getRequestURL("application.snapshot.info", params);
        String response = executeRequest(url);
        com.cloudbees.api.ApplicationSnapshotInfo apiResponse =
                (com.cloudbees.api.ApplicationSnapshotInfo)readResponse(response);
        return apiResponse;
    }

    public ApplicationSnapshotStatusResponse applicationSnapshotUpdate(String snapshotId, Map<String, String> parameters) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("snapshot_id", snapshotId);
        params.put("parameters", createParameter(parameters));
        String url = getRequestURL("application.snapshot.update", params);
        String response = executeRequest(url);
        ApplicationSnapshotStatusResponse apiResponse =
                (ApplicationSnapshotStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationStatusResponse applicationProxyUpdate(String appId, Map<String, String> parameters) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        params.put("parameters", createParameter(parameters));
        String url = getRequestURL("application.proxy.update", params);
        String response = executeRequest(url);
        ApplicationStatusResponse apiResponse =
                (ApplicationStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationConfigUpdateResponse applicationConfigUpdate(String appId, Map<String, String> parameters) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        params.put("parameters", createParameter(parameters));
        String url = getRequestURL("application.config.update", params);
        String response = executeRequest(url);
        ApplicationConfigUpdateResponse apiResponse =
                (ApplicationConfigUpdateResponse)readResponse(response);
        return apiResponse;
    }

    public ServiceResourceBindResponse resourceBind(String fromService, String fromResourceId, String toService, String toResourceId, String alias, Map<String, String> settings) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("to_service", toService);
        params.put("to_resource_id", toResourceId);
        params.put("from_service", fromService);
        params.put("from_resource_id", fromResourceId);
        params.put("alias", alias);
        params.put("settings", createParameter(settings));
        String url = getRequestURL("service.resource.bind", params);
        String response = executeRequest(url);
        ServiceResourceBindResponse apiResponse =
                (ServiceResourceBindResponse)readResponse(response);
        return apiResponse;
    }

    public ServiceResourceUnBindResponse resourceUnBind( String service, String resourceId, String alias) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("alias", alias);
        params.put("service", service);
        params.put("resource_id", resourceId);
        String url = getRequestURL("service.resource.unbind", params);
        String response = executeRequest(url);
        ServiceResourceUnBindResponse apiResponse =
                (ServiceResourceUnBindResponse)readResponse(response);
        return apiResponse;
    }

    public ServiceResourceInfo serviceResourceCreate(String service, String account, String resourceType, String resourceName, Map<String, String> settings) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account);
        params.put("service", service);
        if (resourceType != null)
            params.put("resource_type", resourceType);
        params.put("resource_name", resourceName);
        params.put("settings", createParameter(settings));

        ServiceResourceResponse apiResponse = (ServiceResourceResponse) apiCall("service.resource.create", settings, params);
        return apiResponse.getResource();
    }

    public ServiceResourceInfo serviceResourceUpdate(String service, String resourceId, Map<String, String> settings) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("service", service);
        params.put("resource_id", resourceId);
        params.put("settings", createParameter(settings));

        ServiceResourceResponse apiResponse = (ServiceResourceResponse) apiCall("service.resource.update", settings, params);
        return apiResponse.getResource();
    }

    public ServiceResourceDeleteResponse serviceResourceDelete(String service, String resourceId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("service", service);
        params.put("resource_id", resourceId);
        String url = getRequestURL("service.resource.delete", params);
        String response = executeRequest(url);
        ServiceResourceDeleteResponse apiResponse =
                (ServiceResourceDeleteResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationCreateResponse applicationCreate(String appId, Map<String, String> parameters) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        params.put("parameters", createParameter(parameters));
        String url = getRequestURL("application.create", params);
        String response = executeRequest(url);
        ApplicationCreateResponse apiResponse =
                (ApplicationCreateResponse)readResponse(response);
        return apiResponse;
    }

    public ServerPoolInfo serverPoolCreate(String account, String poolName, Map<String, String> parameters) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account);
        params.put("pool_name", poolName);
        params.put("parameters", createParameter(parameters));
        String url = getRequestURL("server.pool.create", params);
        String response = executeRequest(url);
        ServerPoolInfo apiResponse =
                (ServerPoolInfo)readResponse(response);
        return apiResponse;
    }

    public ServerPoolInfo serverPoolInfo(String poolId) throws Exception
    {
        return serverPoolInfo(poolId, false);
    }
    public ServerPoolInfo serverPoolInfo(String poolId, boolean withApplications) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("pool_id", poolId);
        if (withApplications)
            params.put("with_applications", "true");
        String url = getRequestURL("server.pool.info", params);
        String response = executeRequest(url);
        ServerPoolInfo apiResponse =
                (ServerPoolInfo)readResponse(response);
        return apiResponse;
    }

    public ServerPoolListResponse serverPoolList(String account) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account);
        String url = getRequestURL("server.pool.list", params);
        String response = executeRequest(url);
        ServerPoolListResponse apiResponse =
                (ServerPoolListResponse)readResponse(response);
        return apiResponse;
    }

    public ServerPoolDeleteResponse serverPoolDelete(String poolId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("pool_id", poolId);
        String url = getRequestURL("server.pool.delete", params);
        String response = executeRequest(url);
        ServerPoolDeleteResponse apiResponse =
                (ServerPoolDeleteResponse)readResponse(response);
        return apiResponse;
    }

    public ServerInfo serverCreate(String poolId, Map<String, String> parameters) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("pool_id", poolId);
        params.put("parameters", createParameter(parameters));
        String url = getRequestURL("server.create", params);
        String response = executeRequest(url);
        ServerInfo apiResponse =
                (ServerInfo)readResponse(response);
        return apiResponse;
    }

    public ServerRestoreResponse serverRestore(String serverId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("server_id", serverId);
        String url = getRequestURL("server.restore", params);
        String response = executeRequest(url);
        ServerRestoreResponse apiResponse =
                (ServerRestoreResponse)readResponse(response);
        return apiResponse;
    }

    public ServerInfo serverInfo(String serverId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("server_id", serverId);
        String url = getRequestURL("server.info", params);
        String response = executeRequest(url);
        ServerInfo apiResponse =
                (ServerInfo)readResponse(response);
        return apiResponse;
    }

    public ServerStatusResponse serverStop(String serverId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("server_id", serverId);
        String url = getRequestURL("server.stop", params);
        String response = executeRequest(url);
        ServerStatusResponse apiResponse =
                (ServerStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ServerStatusResponse serverDelete(String serverId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("server_id", serverId);
        String url = getRequestURL("server.delete", params);
        String response = executeRequest(url);
        ServerStatusResponse apiResponse =
                (ServerStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ServerStatusResponse serverDeactivate(String serverId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("server_id", serverId);
        String url = getRequestURL("server.deactivate", params);
        String response = executeRequest(url);
        ServerStatusResponse apiResponse =
                (ServerStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ServiceSubscriptionInvokeInfo serviceSubscriptionInvoke(String service, String subscriptionId, String invoke, Map<String, String> parameters) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("service", service);
        params.put("subscription_id", subscriptionId);
        params.put("invoke", invoke);
        params.put("parameters", createParameter(parameters));

        ServiceSubscriptionInvokeResponse apiResponse = (ServiceSubscriptionInvokeResponse) apiCall("service.subscription.invoke", parameters, params);
        return apiResponse.getInvokeInfo();
    }


    public ServiceResourceInvokeInfo serviceResourceInvoke(String service, String resourceId, String invoke, Map<String, String> parameters) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("service", service);
        params.put("resource_id", resourceId);
        params.put("invoke", invoke);
        params.put("parameters", createParameter(parameters));

        ServiceResourceInvokeResponse apiResponse = (ServiceResourceInvokeResponse) apiCall("service.resource.invoke", parameters, params);
        return apiResponse.getInvokeInfo();
    }

    public ApplicationCreateResponse applicationCreate(String appId, Map<String, String> parameters, Map<String, String> appParameters, Map<String, String> appVariables) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        params.put("parameters", createParameter(parameters));
        params.put("app_parameters", createParameter(appParameters));
        params.put("app_variables", createParameter(appVariables));
        String url = getRequestURL("application.create", params);
        String response = executeRequest(url);
        ApplicationCreateResponse apiResponse =
                (ApplicationCreateResponse)readResponse(response);
        return apiResponse;
    }

    public List<ResourceSettings> applicationResources(String appId, String resourceType, String configType, String environment) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        if (environment != null)
            params.put("environment", environment);
        if (resourceType != null)
            params.put("resource_type", resourceType);
        if (configType != null)
            params.put("config_type", configType);
        String url = getRequestURL("application.resources", params);
        String response = executeRequest(url);
        List<ResourceSettings> apiResponse =
                (List<ResourceSettings>)readResponse(response);
        return apiResponse;
    }

    @Override
    public String executeRequest(String url) throws Exception {
        trace("API call: " + url);
        String response = super.executeRequest(url);
        traceResponse(response);
        return response;
    }

    @Override
    protected String executeUpload(String uploadURL, Map<String, String> params, Map<String, File> files, UploadProgress writeListener) throws Exception {
        if (isVerbose()) {
            String msg = "API call: " + uploadURL;
            if (params != null && params.size() > 0) msg += ",P" + params;
            if (files != null && files.size() > 0) msg += ",F" + files;
            trace(msg);
        }
        return super.executeUpload(uploadURL, params, files, writeListener);
    }

    protected static ApplicationConfiguration getAppConfig(File deployZip, final String[] environments,
                                                           final String[] implicitEnvironments) throws IOException {
        final ApplicationConfiguration applicationConfiguration = new ApplicationConfiguration();

        FileInputStream fin = new FileInputStream(deployZip);
        try {
            ZipHelper.unzipFile(fin, new ZipHelper.ZipEntryHandler() {
                public void unzip(ZipEntry entry, InputStream zis)
                        throws IOException {
                    if (entry.getName().equals("META-INF/stax-application.xml")
                            || entry.getName().equals("WEB-INF/stax-web.xml")
                            || entry.getName().equals("WEB-INF/cloudbees-web.xml")) {
                        AppConfigHelper.load(applicationConfiguration, zis, environments, implicitEnvironments);
                    }
                }
            }, false);
        } finally {
            fin.close();
        }

        return applicationConfiguration;
    }

    protected String createParameter(Map<String, String> parameters) {
        if (parameters == null) {
            parameters = new HashMap<String, String>();
        }
        JSONObject jsonObject = new JSONObject(parameters);
        return jsonObject.toString();
    }

    public void tailLog(String appId, String logName, OutputStream out) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        params.put("log_name", logName);
        String url = getRequestURL("tail", params, false);
        trace("API call: " + url);
        InputStream input = executeCometRequest(url);

        byte[] bytes = new byte[1024];
        int numRead = input.read(bytes);
        while (numRead != -1) {
            out.write(bytes, 0, numRead);
            numRead = input.read(bytes);
        }
    }

    protected Object apiCall(String apiMethod, Map<String, String> settings, Map<String, String> params) throws Exception {
        Map<String, File> fileParams = new HashMap<String, File>();
        if (settings != null) {
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                String value = entry.getValue().toLowerCase();
                int idx = value.startsWith("file://") ? 7 : 0;
                if (idx == 0)
                    idx = value.startsWith("files://") ? 8 : 0;
                if (idx > 0) {
                    File file = new File(entry.getValue().substring(idx));
                    fileParams.put("FILE." + entry.getKey(), file);
                    params.put("FILENAME." + entry.getKey(), file.getName());
                }
            }
        }
        String response;
        // If files are to be uploaded use POST to upload
        if (fileParams.size() > 0) {
            String url = getApiUrl(apiMethod).toString();
            params.put("action", apiMethod);
            response = executeUpload(url, params, fileParams, new UploadProgress() {
                public void handleBytesWritten(long l, long l1, long l2) {}
            });
        } else {
            String url = getRequestURL(apiMethod, params);
            response = executeRequest(url);
        }
        return readResponse(response);
    }

    public String call(String action, Map<String, String> params) throws Exception {
        String url = getRequestURL(action, params);
        String response = executeRequest(url);
        return response;
    }

    protected XStream getXStream() throws Exception {
        XStream xstream;
        if (format.equals("json")) {
            xstream = new XStream(new JettisonMappedXmlDriver()) {
                protected MapperWrapper wrapMapper(MapperWrapper next) {
                    return new MapperWrapper(next) {
                        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                            return definedIn != Object.class && super.shouldSerializeMember(definedIn, fieldName);
                        }

                    };
                }
            };
        } else if (format.equals("xml")) {
            xstream = new XStream() {
                protected MapperWrapper wrapMapper(MapperWrapper next) {
                    return new MapperWrapper(next) {
                        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                            return definedIn != Object.class && super.shouldSerializeMember(definedIn, fieldName);
                        }

                    };
                }
            };
        } else {
            throw new Exception("Unknown format: " + format);
        }

        xstream.processAnnotations(SayHelloResponse.class);
        xstream.processAnnotations(ApplicationGetSourceUrlResponse.class);
        xstream.processAnnotations(ApplicationDeleteResponse.class);
        xstream.processAnnotations(ApplicationDeployResponse.class);
        xstream.processAnnotations(ApplicationDeployArchiveResponse.class);
        xstream.processAnnotations(ApplicationInstallResponse.class);
        xstream.processAnnotations(ApplicationInfo.class);
        xstream.processAnnotations(ApplicationInfoResponse.class);
        xstream.processAnnotations(ApplicationListResponse.class);
        xstream.processAnnotations(ApplicationRestartResponse.class);
        xstream.processAnnotations(ApplicationStatusResponse.class);
        xstream.processAnnotations(ApplicationSetMetaResponse.class);
        xstream.processAnnotations(ApplicationCheckSumsResponse.class);
        xstream.processAnnotations(ApplicationScaleResponse.class);
        xstream.processAnnotations(ApplicationSnapshotInfo.class);
        xstream.processAnnotations(ApplicationSnapshotListResponse.class);
        xstream.processAnnotations(ApplicationSnapshotStatusResponse.class);
        xstream.processAnnotations(DatabaseCreateResponse.class);
        xstream.processAnnotations(DatabaseSetPasswordResponse.class);
        xstream.processAnnotations(DatabaseDeleteResponse.class);
        xstream.processAnnotations(DatabaseInfo.class);
        xstream.processAnnotations(DatabaseInfoResponse.class);
        xstream.processAnnotations(DatabaseListResponse.class);
        xstream.processAnnotations(DatabaseSnapshotInfo.class);
        xstream.processAnnotations(DatabaseSnapshotListResponse.class);
        xstream.processAnnotations(DatabaseSnapshotDeployResponse.class);
        xstream.processAnnotations(DatabaseSnapshotDeleteResponse.class);
        xstream.processAnnotations(ErrorResponse.class);
        xstream.processAnnotations(AccountKeysResponse.class);
        xstream.processAnnotations(AccountInfo.class);
        xstream.processAnnotations(AccountListResponse.class);
        xstream.processAnnotations(ApplicationJarHashesResponse.class);
        xstream.processAnnotations(ConfigurationParametersResponse.class);
        xstream.processAnnotations(ConfigurationParametersUpdateResponse.class);
        xstream.processAnnotations(ConfigurationParametersDeleteResponse.class);
        xstream.processAnnotations(ServiceSubscriptionResponse.class);
        xstream.processAnnotations(ServiceSubscriptionListResponse.class);
        xstream.processAnnotations(ServiceSubscriptionDeleteResponse.class);
        xstream.processAnnotations(ServiceResourceInfo.class);
        xstream.processAnnotations(ServiceResourceResponse.class);
        xstream.processAnnotations(ServiceResourceListResponse.class);
        xstream.processAnnotations(ServiceResourceBindingListResponse.class);
        xstream.processAnnotations(ResourceBindingInfo.class);
        xstream.processAnnotations(AccountRegionInfo.class);
        xstream.processAnnotations(AccountRegionListResponse.class);
        xstream.processAnnotations(ApplicationInstanceInfo.class);
        xstream.processAnnotations(ApplicationInstanceListResponse.class);
        xstream.processAnnotations(ApplicationInstanceStatusResponse.class);
        xstream.processAnnotations(ApplicationInstanceInvokeResponse.class);
        xstream.processAnnotations(ApplicationConfigUpdateResponse.class);
        xstream.processAnnotations(ServiceResourceDeleteResponse.class);
        xstream.processAnnotations(ServiceResourceBindResponse.class);
        xstream.processAnnotations(ServiceResourceUnBindResponse.class);
        xstream.processAnnotations(RepositoryInfo.class);
        xstream.processAnnotations(CIInfo.class);
        xstream.processAnnotations(ApplicationCreateResponse.class);
        xstream.processAnnotations(ServerPoolInfo.class);
        xstream.processAnnotations(ServerPoolListResponse.class);
        xstream.processAnnotations(ServerPoolDeleteResponse.class);
        xstream.processAnnotations(ServerInfo.class);
        xstream.processAnnotations(ServerStatusResponse.class);
        xstream.processAnnotations(ServerRestoreResponse.class);
        xstream.processAnnotations(ServerReinitResponse.class);
        xstream.processAnnotations(ServiceResourceInvokeInfo.class);
        xstream.processAnnotations(ServiceSubscriptionInvokeInfo.class);
        xstream.processAnnotations(ServiceResourceResponse.class);
        xstream.processAnnotations(ServiceResourceInvokeResponse.class);
        xstream.processAnnotations(ServiceSubscriptionInvokeResponse.class);
        xstream.processAnnotations(ServiceListResponse.class);
        xstream.processAnnotations(ParameterSettings.class);
        xstream.processAnnotations(ResourceSettings.class);
        xstream.processAnnotations(DatabaseBackupInfo.class);
        xstream.processAnnotations(DatabaseBackupListResponse.class);
        xstream.processAnnotations(DatabaseBackupResponse.class);
        xstream.processAnnotations(DatabaseClusterInfo.class);
        xstream.processAnnotations(DatabaseClusterListResponse.class);

        // Hack to fix backward compatibility
        xstream.alias("net.stax.api.ApplicationStatusResponse", ApplicationStatusResponse.class);
        xstream.alias("net.stax.api.ApplicationSetMetaResponse", ApplicationSetMetaResponse.class);

        // BeesClient can be subtyped to offer more commands,
        // yet those may live in separate classloader.
        // use this.getClass().getClassLoader() to ensure
        // that all the request/response classes resolve.
        xstream.setClassLoader(getClass().getClassLoader());

        return xstream;
    }

    protected Object readResponse(String response) throws Exception {
        Object obj = getXStream().fromXML(response);
        if (obj instanceof ErrorResponse) {
            throw new BeesClientException((ErrorResponse) obj);
        }
        return obj;
    }

    public static String encodePassword(String password, String version) {
        if ("0.1".equals(version)) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA");
                byte[] passwordDigest = sha.digest(password.getBytes("UTF8"));
                String result = new BigInteger(1, passwordDigest).toString(16);
                if (result.length() < 32) {
                    char[] padded = new char[32];
                    char[] raw = result.toCharArray();
                    Arrays.fill(padded, 0, 32 - raw.length, '0');
                    System.arraycopy(raw, 0, padded, 32 - raw.length, raw.length);
                    result = new String(padded);
                }
                return result;
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException("JVM is supposed to provide SHA instance of MessageDigest", e);
            } catch (UnsupportedEncodingException e) {
                throw new IllegalStateException("JVM is supposed to provide UTF-8 character encoding", e);
            }
        } else {
            return password;
        }
    }

    public void mainCall(String[] args) throws Exception {
        Map<String, String> params = new HashMap<String, String>();
        int argIndex = 0;
        if (argIndex < args.length) {
            String action = args[argIndex++];
            for (; argIndex < args.length; argIndex++) {
                String arg = args[argIndex];
                String[] pair = arg.split("=", 2);
                if (pair.length < 2) {
                    throw new BeesClient.UsageError("Malformed call parameter pair: " +
                            arg);
                }
                params.put(pair[0], pair[1]);
            }
            String response = call(action, params);
            System.out.println(response);
        } else {
            throw new BeesClient.UsageError("Missing required action argument");
        }
    }

    public void main(String[] args) throws Exception {
        int argIndex = 0;
        Map<String, String> options = new HashMap<String, String>();
        for (; argIndex < args.length; argIndex++) {
            String arg = args[argIndex];
            if (arg.startsWith("-")) {
                if (arg.equals("--call") || arg.equals("-c")) {
                    options.put("operation", arg);
                } else if (arg.equals("--username") || arg.equals("-u")) {
                    options.put("username", arg);
                } else if (arg.equals("--password") || arg.equals("-p")) {
                    options.put("password", arg);
                } else if (arg.equals("--url") || arg.equals("-u")) {
                    options.put("url", arg);
                } else {
                    throw new BeesClient.UsageError("Unsupported option: " + arg);
                }
            } else {
                break;
            }
        }

        String operation = getRequiredOption("operation", options);
        BeesClient client =
                new BeesClient(getRequiredOption("url", options),
                        getRequiredOption("username", options), getRequiredOption(
                        "password", options), "0.1", "1.0");

        if (operation.equals("call")) {
            String[] subArgs = new String[args.length - argIndex];
            for (int i = 0; i < subArgs.length; i++) {
                subArgs[i] = args[argIndex++];
            }
            client.main(subArgs);
        }
    }

    private static String getRequiredOption(String optionName,
                                            Map<String, String> options) throws BeesClient.UsageError {
        if (options.containsKey(optionName)) {
            return options.get(optionName);
        } else {
            throw new BeesClient.UsageError("Missing required flag: --" + optionName);
        }
    }

    private static File asFile(String filePath) {
        return filePath == null ? null : new File(filePath);
    }

    public static class UsageError extends Exception {
        UsageError(String reason) {
            super(reason);
        }
    }

    class PatchMethod extends EntityEnclosingMethod {
        PatchMethod() {
        }

        PatchMethod(String uri) {
            super(uri);
        }

        @Override
        public String getName() {
            return "PATCH";
        }
    }

    <T> T assertNotNull(T value, String arg) {
        if (value==null)
            throw new IllegalArgumentException("Null is not a valid value for the '"+arg+"' argument");
        return value;
    }
}

