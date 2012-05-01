/*
 * Copyright 2010-2011, CloudBees Inc.
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

import com.cloudbees.upload.ArchiveUtils;
import com.cloudbees.upload.JarUtils;
import com.cloudbees.utils.AppConfigHelper;
import com.cloudbees.utils.ZipHelper;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.mapper.MapperWrapper;
import org.codehaus.jettison.json.JSONObject;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * @author Fabian Donze
 */
public class BeesClient extends BeesClientBase
{
    static Logger logger = Logger.getLogger(BeesClient.class.getSimpleName());


    public BeesClient(BeesClientConfiguration beesClientConfiguration) {
        super(beesClientConfiguration);
    }
    public BeesClient(String server, String apikey, String secret,
                      String format, String version)
    {
        // TODO: this encodePassword is considered harmful as it creates assymetry between two constructors
        super(server, apikey, encodePassword(secret, version), format,
            version);
    }

    public SayHelloResponse sayHello(String message) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("message", message);
        String url = getRequestURL("say.hello", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        SayHelloResponse helloResponse =
            (SayHelloResponse)readResponse(response);
        return helloResponse;
    }

    public ApplicationGetSourceUrlResponse applicationGetSourceUrl(
        String appId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.getSourceUrl", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationGetSourceUrlResponse apiResponse =
            (ApplicationGetSourceUrlResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationDeleteResponse applicationDelete(String appId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.delete", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationDeleteResponse apiResponse =
            (ApplicationDeleteResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationRestartResponse applicationRestart(String appId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.restart", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationRestartResponse apiResponse =
            (ApplicationRestartResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationStatusResponse applicationStart(String appId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.start", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationStatusResponse apiResponse =
            (ApplicationStatusResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationStatusResponse applicationStop(String appId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.stop", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationStatusResponse apiResponse =
            (ApplicationStatusResponse)readResponse(response);
        return apiResponse;
    }

    /**
     * Returns all the applications in all the account sthat you belong to.
     *
     * Short-hand for {@code applicationList(null)}.
     */
    public ApplicationListResponse applicationList() throws Exception
    {
        return applicationList(null);
    }

    /**
     * Returns all the applications in the specified account.
     *
     * @param account
     *      if null, returns all the applications from all the accounts that you belong to.
     * @since 1.1.3
     */
    public ApplicationListResponse applicationList(String account) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        if (account != null)
            params.put("account", account);
        String url = getRequestURL("application.list", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationListResponse apiResponse =
            (ApplicationListResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationInfo applicationInfo(String appId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.info", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationInfoResponse apiResponse =
            (ApplicationInfoResponse)readResponse(response);
        return apiResponse.getApplicationInfo();
    }

    public ApplicationSetMetaResponse applicationSetMeta(String appId,
        Map<String, String> metaAttrs) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.putAll(metaAttrs);
        params.put("app_id", appId);
        String url = getRequestURL("application.setMeta", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        ApplicationSetMetaResponse apiResponse =
            (ApplicationSetMetaResponse)readResponse(response);
        return apiResponse;
    }

    /**
     * @deprecated
     */
    public ApplicationJarHashesResponse applicationJarCrcs(String appId, Map<String, String> hashes) throws Exception
    {
        return applicationJarHashes(appId, hashes);
    }

    public ApplicationJarHashesResponse applicationJarHashes(String appId, Map<String, String> hashes) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        params.put("hashes", createParameter(hashes));

        String url = getApiUrl("application.jarHashes").toString();
        params.put("action", "application.jarHashes");
        // use the upload method (POST) to handle the potentially large "hashes" parameter payload
        trace("API call: " + url);
        String response = executeUpload(url, params, new HashMap<String, File>(), null);
        ApplicationJarHashesResponse apiResponse =
            (ApplicationJarHashesResponse)readResponse(response);
        return apiResponse;
    }

    /**
     * @deprecated use {@link #applicationDeployEar(String,String,String,java.io.File,java.io.File,UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployEar(
        String appId, String environment, String description, String earFile,
        String srcFile, UploadProgress progress) throws Exception
    {
        return applicationDeployEar(appId, environment, description,
                asFile(earFile), asFile(srcFile), progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployEar(
        String appId, String environment, String description, File earFile,
        File srcFile, UploadProgress progress) throws Exception
    {
        String archiveType = "ear";
        return applicationDeployArchive(appId, environment, description,
                earFile, srcFile, archiveType, false, progress);
    }

    /**
     * @deprecated use {@link #applicationDeployWar(String,String,String,File,File,UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployWar(
        String appId, String environment, String description, String warFile,
        String srcFile, UploadProgress progress) throws Exception
    {
        return applicationDeployWar(appId, environment, description, asFile(warFile),
                asFile(srcFile), progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployWar(
        String appId, String environment, String description, File warFile,
        File srcFile, UploadProgress progress) throws Exception
    {
        return applicationDeployWar(appId, environment, description, warFile,
                srcFile, true, progress);
    }

    /**
     * @deprecated use {@link #applicationDeployWar(String,String,String,File,File,boolean,UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployWar(
        String appId, String environment, String description, String warFile,
        String srcFile, boolean deltaDeploy, UploadProgress progress) throws Exception
    {
        return applicationDeployWar(appId, environment, description,
                asFile(warFile), asFile(srcFile), deltaDeploy, progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployWar(
        String appId, String environment, String description, File warFile,
        File srcFile, boolean deltaDeploy, UploadProgress progress) throws Exception
    {
        String archiveType = "war";
        return applicationDeployArchive(appId, environment, description,
                warFile, srcFile, archiveType, deltaDeploy, progress);
    }

    /**
     * @deprecated use {@link #applicationDeployArchive(String,String,String,File,File,String,UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, String earFile,
            String srcFile, String archiveType, UploadProgress progress) throws Exception
    {
        return applicationDeployArchive(appId, environment, description, asFile(earFile), asFile(srcFile), archiveType, progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, File earFile,
            File srcFile, String archiveType, UploadProgress progress) throws Exception
    {
        return applicationDeployArchive(appId, environment, description, earFile, srcFile, archiveType, false, progress);
    }

    /**
     * @deprecated use {@link #applicationDeployArchive(String,String,String,File,File,String,boolean,UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, String earFile,
            String srcFile, String archiveType, boolean deltaDeploy, UploadProgress progress) throws Exception
    {
        return applicationDeployArchive(appId, environment, description, asFile(earFile), asFile(srcFile), archiveType, deltaDeploy, progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, File earFile,
            File srcFile, String archiveType, boolean deltaDeploy, UploadProgress progress) throws Exception
    {
        return applicationDeployArchive(appId, environment, description, earFile, srcFile, archiveType, deltaDeploy, null, progress);
    }

    /**
     * @deprecated use {@link #applicationDeployArchive(String,String,String,File,File,String,boolean,Map,UploadProgress)}
     */
    @Deprecated
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, String earFile,
            String srcFile, String archiveType, boolean deltaDeploy, Map<String, String> parameters, UploadProgress progress) throws Exception
    {
        return applicationDeployArchive(appId, environment, description, asFile(earFile), asFile(srcFile), archiveType, deltaDeploy, parameters, progress);
    }

    /**
     * @since 1.1.4
     */
    public ApplicationDeployArchiveResponse applicationDeployArchive(
            String appId, String environment, String description, File earFile,
            File srcFile, String archiveType, boolean deltaDeploy, Map<String, String> parameters, UploadProgress progress) throws Exception
    {
        return applicationDeployArchive(new ApplicationDeployArgs.Builder(appId)
                .environment(environment).description(description)
                .deployPackage(earFile, archiveType).srcFile(srcFile)
                .incrementalDeployment(deltaDeploy).withParams(parameters)
                .withProgressFeedback(progress).build());
    }

    public ApplicationDeployArchiveResponse applicationDeployArchive(ApplicationDeployArgs args) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, File> fileParams = new HashMap<String, File>();
        params.put("app_id", args.appId);

        File archiveFile = args.archiveFile;

        // Currently only support WAR file for delta upload
        boolean deployDelta = false;
        boolean deployJarDelta = false;
        // Create delta deploy File
        if (args.deltaDeploy && args.archiveType.equals("war")) {
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
                archiveFile = ArchiveUtils.createDeltaWarFile(applicationCheckSumsResponse.getCheckSums(), archiveFile, archiveFile.getParent());
                deployDelta = true;
            }
        }

        if (args.deltaDeploy && args.archiveType.equals("war")) {
            trace("Get existing jar hashes");
            ApplicationJarHashesResponse applicationJarHashesResponse = applicationJarHashes(args.appId, JarUtils.getJarHashes(archiveFile));
            if (applicationJarHashesResponse.getJarHash().size() == 0) {
                trace("No existing jars");
            } else {
                trace("Creating Delta2 archive for: " + archiveFile);
                File archiveFile2 = JarUtils.createDeltaWarFile(applicationJarHashesResponse.getJarHash(), archiveFile, archiveFile.getParent());
                // Delete the old delta archive
                if (deployDelta) {
                    archiveFile.delete();
                }
                archiveFile = archiveFile2;
                deployJarDelta = true;
            }
        }

        if (deployDelta || deployJarDelta)
            trace("Uploading delta archive: " + archiveFile);

        File archiveFileSrc = args.srcFile;
        long uploadSize = archiveFile.length();
        if (archiveFileSrc != null)
            uploadSize += archiveFileSrc.length();

        fileParams.put("archive", archiveFile);
        params.put("archive_type", args.archiveType);
        
        params.put("create", Boolean.valueOf(args.create).toString());

        if (args.environment != null)
            params.put("environment", args.environment);

        if (args.description != null)
            params.put("description", args.description);

        if (archiveFileSrc != null)
            fileParams.put("src", archiveFileSrc);

        params.put("parameters", createParameter(args.parameters));
        params.put("variables", createParameter(args.variables));

        // extend the deploy invocation timeout to 4 hours
        long expireTime = System.currentTimeMillis() + 4 * 60 * 60 * 1000;
        params.put("expires", new Long(expireTime / 1000).toString());

        String url = getApiUrl("application.deployArchive").toString();
        params.put("action", "application.deployArchive");
        trace("API call: " + url);
        String response = executeUpload(url, params, fileParams, args.progress);
        try {
            ApplicationDeployArchiveResponse apiResponse =
                (ApplicationDeployArchiveResponse)readResponse(response);

            return apiResponse;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Invalid application deployment response: " + args.appId, e);
            logger.log(Level.FINE, "Deploy response trace: " + response);
            throw e;
        } finally {
            // Delete the delta archive file
            if (deployDelta || deployJarDelta)
                archiveFile.delete();
        }
    }

    public ApplicationCheckSumsResponse applicationCheckSums(String appId) throws Exception
    {
        return applicationCheckSums(appId, true);
    }
    public ApplicationCheckSumsResponse applicationCheckSums(String appId, boolean traceResponse) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("app_id", appId);
        String url = getRequestURL("application.checkSums", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        if (traceResponse)
            traceResponse(response);
        ApplicationCheckSumsResponse apiResponse =
            (ApplicationCheckSumsResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationScaleResponse applicationScale( String appId, int unit) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("unit", ""+unit);
        params.put("app_id", appId);
        String url = getRequestURL("application.scale", params);
        String response = executeRequest(url);
        ApplicationScaleResponse apiResponse =
            (ApplicationScaleResponse)readResponse(response);
        return apiResponse;
    }

    public DatabaseCreateResponse databaseCreate(String domain, String dbId,
        String username, String password) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        params.put("database_username", username);
        params.put("database_password", password);
        params.put("domain", domain);
        String url = getRequestURL("database.create", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        DatabaseCreateResponse apiResponse =
            (DatabaseCreateResponse)readResponse(response);
        return apiResponse;
    }

    public DatabaseDeleteResponse databaseDelete(String dbId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        String url = getRequestURL("database.delete", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        DatabaseDeleteResponse apiResponse =
            (DatabaseDeleteResponse)readResponse(response);
        return apiResponse;
    }

    public DatabaseInfo databaseInfo(String dbId, boolean fetchPassword) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        params.put("fetch_password", ((Boolean)fetchPassword).toString());
        String url = getRequestURL("database.info", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        DatabaseInfoResponse apiResponse =
            (DatabaseInfoResponse)readResponse(response);
        return apiResponse.getDatabaseInfo();
    }

    /**
     * Returns all the databases in all the account sthat you belong to.
     *
     * Short-hand for {@code databaseList(null)}.
     */
    public DatabaseListResponse databaseList() throws Exception
    {
        return databaseList(null);
    }

    /**
     * Returns all the databases in the specified account.
     *
     * @param account
     *      if null, returns all the databases from all the accounts that you belong to.
     * @since 1.1.3
     */
    public DatabaseListResponse databaseList(String account) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        if (account != null)
            params.put("account", account);
        String url = getRequestURL("database.list", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        DatabaseListResponse apiResponse =
            (DatabaseListResponse)readResponse(response);
        return apiResponse;
    }

    public DatabaseSetPasswordResponse databaseSetPassword(String dbId, String password) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        params.put("database_password", password);
        String url = getRequestURL("database.setPassword", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        DatabaseSetPasswordResponse apiResponse =
            (DatabaseSetPasswordResponse)readResponse(response);
        return apiResponse;
    }

    public DatabaseSnapshotListResponse databaseSnapshotList(String dbId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        String url = getRequestURL("database.snapshot.list", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        DatabaseSnapshotListResponse apiResponse =
            (DatabaseSnapshotListResponse)readResponse(response);
        return apiResponse;
    }

    public DatabaseSnapshotDeleteResponse databaseSnapshotDelete(String dbId, String snapshotId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        params.put("snapshot_id", snapshotId);
        String url = getRequestURL("database.snapshot.delete", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        DatabaseSnapshotDeleteResponse apiResponse =
            (DatabaseSnapshotDeleteResponse)readResponse(response);
        return apiResponse;
    }

    public DatabaseSnapshotDeployResponse databaseSnapshotDeploy(String dbId, String snapshotId) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        params.put("snapshot_id", snapshotId);
        String url = getRequestURL("database.snapshot.deploy", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        DatabaseSnapshotDeployResponse apiResponse =
            (DatabaseSnapshotDeployResponse)readResponse(response);
        return apiResponse;
    }

    public DatabaseSnapshotInfo databaseSnapshotCreate(String dbId, String snapshotTitle) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("database_id", dbId);
        if (snapshotTitle != null)
            params.put("snapshot_title", snapshotTitle);
        String url = getRequestURL("database.snapshot.create", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        DatabaseSnapshotInfo apiResponse =
            (DatabaseSnapshotInfo)readResponse(response);
        return apiResponse;
    }

    public AccountKeysResponse accountKeys(String domain, String user, String password) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user", user);
        params.put("password", password);
        if (domain != null) params.put("domain", domain);
        String url = getRequestURL("account.keys", params);
        String response = executeRequest(url);
        AccountKeysResponse apiResponse =
            (AccountKeysResponse)readResponse(response);
        return apiResponse;
    }

    public AccountListResponse accountList() throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        String url = getRequestURL("account.list", params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        AccountListResponse apiResponse =
            (AccountListResponse)readResponse(response);
        return apiResponse;
    }

    public ApplicationConfiguration getApplicationConfiguration(String warFilePath, String account, String[] environments) throws Exception {
        ApplicationConfiguration appConfig;
        File deployFile = asFile(warFilePath);
        if (deployFile.exists()) {
            appConfig = getAppConfig(deployFile, environments, new String[] { "deploy" });
        } else {
            throw new IllegalArgumentException("File not found: " + warFilePath);
        }

        String appid = appConfig.getApplicationId();
        if (appid == null || appid.equals(""))
            throw new IllegalArgumentException("No application id specified");

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

    public ConfigurationParametersUpdateResponse configurationParametersUpdate(String resourceId, String configType, File resourceFile) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        Map<String, File> fileParams = new HashMap<String, File>();

        params.put("resource_id", resourceId);
        params.put("config_type", configType);
        fileParams.put("resources", resourceFile);

        String url = getApiUrl("configuration.parameters.update").toString();
        params.put("action", "configuration.parameters.update");
        // use the upload method (POST) to handle the potentially large resource list
        String response = executeUpload(url, params, fileParams, null);
        ConfigurationParametersUpdateResponse apiResponse =
            (ConfigurationParametersUpdateResponse)readResponse(response);
        return apiResponse;
    }

    public ConfigurationParametersDeleteResponse configurationParametersDelete(String resourceId, String configType) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("resource_id", resourceId);
        params.put("config_type", configType);
        String url = getRequestURL("configuration.parameters.delete", params);
        String response = executeRequest(url);
        ConfigurationParametersDeleteResponse apiResponse =
            (ConfigurationParametersDeleteResponse)readResponse(response);
        return apiResponse;
    }

    public ConfigurationParametersResponse configurationParameters(String resourceId, String configType) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("resource_id", resourceId);
        params.put("config_type", configType);
        String url = getRequestURL("configuration.parameters", params);
        String response = executeRequest(url);
        ConfigurationParametersResponse apiResponse =
            (ConfigurationParametersResponse)readResponse(response);
        return apiResponse;
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

    protected String createParameter(Map<String,String>parameters) {
        if (parameters == null)
            parameters = new HashMap<String, String>();
        JSONObject jsonObject = new JSONObject(parameters);
        return jsonObject.toString();
    }

    public void tailLog(String appId, String logName, OutputStream out) throws Exception
    {
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

    public String call(String action, Map<String, String> params) throws Exception
    {
        String url = getRequestURL(action, params);
        trace("API call: " + url);
        String response = executeRequest(url);
        traceResponse(response);
        return response;
    }

    protected XStream getXStream() throws Exception {
        XStream xstream;
        if (format.equals("json")) {
            xstream = new XStream(new JettisonMappedXmlDriver()) {
                protected MapperWrapper wrapMapper(MapperWrapper next) {
                    return new MapperWrapper(next) {
                        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                            return definedIn != Object.class ? super.shouldSerializeMember(definedIn, fieldName) : false;
                        }

                    };
                }
            };
        } else if (format.equals("xml")) {
            xstream = new XStream() {
                protected MapperWrapper wrapMapper(MapperWrapper next) {
                    return new MapperWrapper(next) {
                        public boolean shouldSerializeMember(Class definedIn, String fieldName) {
                            return definedIn != Object.class ? super.shouldSerializeMember(definedIn, fieldName) : false;
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

    protected Object readResponse(String response) throws Exception
    {
        Object obj = getXStream().fromXML(response);
        if (obj instanceof ErrorResponse) {
            throw new BeesClientException((ErrorResponse)obj);
        }
        return obj;
    }

    public static String encodePassword(String password, String version)
    {
        if (version.equals("0.1")) {
            try {
                MessageDigest sha = MessageDigest.getInstance("SHA");
                byte[] shaBytes = sha.digest(password.getBytes("UTF8"));
                StringBuffer hex = new StringBuffer();
                for (int i = 0; i < shaBytes.length; ++i) {
                    hex.append(Integer.toHexString(
                        (shaBytes[i] & 0xFF) | 0x100).substring(1, 3));
                }

                return hex.toString();
            } catch (NoSuchAlgorithmException e) {
            } catch (UnsupportedEncodingException e) {
            }
            return null;
        } else
            return password;
    }

    public void mainCall(String[] args) throws Exception
    {
        Map<String, String> params = new HashMap<String, String>();
        int argIndex = 0;
        if (argIndex < args.length) {
            String action = args[argIndex++];
            for (; argIndex < args.length; argIndex++) {
                String arg = args[argIndex];
                String[] pair = arg.split("=", 2);
                if (pair.length < 2)
                    throw new BeesClient.UsageError("Marlformed call parameter pair: " +
                        arg);
                params.put(pair[0], pair[1]);
            }
            String response = call(action, params);
            System.out.println(response);
        } else
            throw new BeesClient.UsageError("Missing required action argument");
    }

    public void main(String[] args) throws Exception
    {
        int argIndex = 0;
        Map<String, String> options = new HashMap<String, String>();
        for (; argIndex < args.length; argIndex++) {
            String arg = args[argIndex];
            if (arg.startsWith("-")) {
                if (arg.equals("--call") || arg.equals("-c"))
                    options.put("operation", arg);
                else if (arg.equals("--username") || arg.equals("-u"))
                    options.put("username", arg);
                else if (arg.equals("--password") || arg.equals("-p"))
                    options.put("password", arg);
                else if (arg.equals("--url") || arg.equals("-u"))
                    options.put("url", arg);
                else
                    throw new BeesClient.UsageError("Unsupported option: " + arg);
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
        Map<String, String> options) throws BeesClient.UsageError
    {
        if (options.containsKey(optionName))
            return options.get(optionName);
        else
            throw new BeesClient.UsageError("Missing required flag: --" + optionName);
    }

    private static File asFile(String filePath) {
        return filePath == null ? null : new File(filePath);
    }

    public static class UsageError extends Exception
    {
        UsageError(String reason)
        {
            super(reason);
        }
    }
}

