package com.cloudbees.api.cr;

import com.cloudbees.api.oauth.OauthToken;
import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Client-side representation of a remote cloud resource.
 *
 * @author Kohsuke Kawaguchi
 */
public class CloudResource {
    /**
     * URL of the cloud resource. It is always normalized to end with '/' so that relative URL
     * against this URL can be always resolved consistently.
     */
    private final URL url;

    /**
     * The value to set into the Authorize header.
     */
    private final Credential credential;

    private volatile Set<String> types;
    private volatile ObjectNode payload;

    public CloudResource(URL url, Credential credential) {
        String e = url.toExternalForm();
        if (!e.endsWith("/")) {
            try {
                url = new URL(e +'/');
            } catch (MalformedURLException x) {
                throw new Error(x);
            }
        }
        this.url = url;
        this.credential = credential;
    }

    /**
     * A cloud resource is identified by its URL, and to talk to it we need an OAuth access token
     */
    public static CloudResource fromOAuthToken(URL url, String oauthAccessToken) {
        return new CloudResource(url,Credential.oauth(oauthAccessToken));
    }

    public static CloudResource fromOAuthToken(URL url, OauthToken token) {
        return fromOAuthToken(url,token.accessToken);
    }

    /**
     * URL of this cloud resource.
     */
    public URL getUrl() {
        return url;
    }

    public Credential getCredential() {
        return credential;
    }

    /**
     * Creates a client-side wrapper to access this cloud resource through the specified cloud resource type.
     */
    public <T extends CloudResourceFacet> T as(Class<T> facet) throws IOException {
        if (hasType(facet))
            return coerce(facet);
        throw new ClassCastException(String.format("%s is not of type: %s. Valid types are %s", url, facet, types));
    }

    /**
     * Creates a client-side wrapper to access this cloud resource through the specified cloud resource type.
     *
     * Unlike {@link #as(Class)}, this version does not check if the cloud resource
     * actually advertises the specified cloud resource type.
     */
    public <T extends CloudResourceFacet> T coerce(Class<T> facet) throws IOException {
        try {
            return facet.cast(facet.getConstructor(CloudResource.class).newInstance(this));
        } catch (InstantiationException e) {
            throw new Error(e);
        } catch (IllegalAccessException e) {
            throw new Error(e);
        } catch (InvocationTargetException e) {
            throw new Error(e);
        } catch (NoSuchMethodException e) {
            throw new Error(e);
        }
    }

    /**
     * Does this cloud resource implement the specified type?
     */
    public <T extends CloudResourceFacet> boolean hasType(Class<T> facet) throws IOException {
        CloudResourceType a = facet.getAnnotation(CloudResourceType.class);
        if (a==null)
            throw new IllegalArgumentException(facet+" isn't annotated with @CloudResourceType");
        return hasType(a.value());
    }

    /**
     * Does this cloud resource implement the specified type?
     */
    public boolean hasType(String fullyQualifiedTypeName) throws IOException {
        if (types==null)
            retrieve();
        return types.contains(fullyQualifiedTypeName);
    }

    /**
     * Under some limited circumstances, you get a list of types of another cloud resource
     * when you get a reference to it. In such a case, use this method to inform {@link CloudResource}
     * about its types. This will save an unnecessary roundtrip.
     */
    public void setTypes(Collection<String> types) {
        this.types = new HashSet<String>(types);
    }

    /**
     * Retrieves the current state of the CloudResource object as JSON DOM node.
     *
     * @param force
     *      if false, cached value is used if available.
     */
    public ObjectNode retrieve(boolean force) throws IOException {
        if (force || payload==null) {
            HttpURLConnection con = connect();
            // TODO: we need to honor encoding in the Content-Type header, instead of letting Jackson guess it
            payload = (ObjectNode)MAPPER.readTree(decorateResponseStream(con.getInputStream()));
        }
        return payload;
    }

    public ObjectNode retrieve() throws IOException {
        return retrieve(false);
    }

    /**
     * Retrieves the current state of the cloud resource and data-bind it to the given object via Jackson
     */
    public <T> T retrieve(Class<T> type, boolean force) throws IOException {
        return MAPPER.readValue(retrieve(force),type);
    }

    public <T> T retrieve(Class<T> type) throws IOException {
        return retrieve(type,false);
    }

    public void delete() throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        credential.authorizeRequest(con);
        con.setRequestProperty("Accept", CONTENT_TYPE);
        con.setRequestMethod("DELETE");
        con.setRequestProperty("Accept", CONTENT_TYPE);
        dumpRequestHeaders(con);
        con.getInputStream().close();
        checkError(con);
    }


    /**
     * Makes a POST request with the specified request payload, then return the data-bound JSON object in the specified 'response' type.
     *
     * @param path
     *      Normally relative URL (from the URL of the cloud resource) that indicates the endpoint to POST to.
     */
    public <T> T post(String path, Object request, Class<T> responseType) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL(url,path).openConnection();
        credential.authorizeRequest(con);
        sendRequest(request, con);

        if (responseType!=null)
            return MAPPER.readValue(decorateResponseStream(con.getInputStream()),responseType);
        else {
            con.getInputStream().close();
            return null;
        }
    }

    /**
     * Makes a POST request with the specified request payload, then expect 201 status that reports the location
     * of the newly created resource.
     *
     * @param path
     *      Normally relative URL (from the URL of the cloud resource) that indicates the endpoint to POST to.
     */
    public CloudResource create(String path, Object request) throws IOException {
        URL ep = new URL(url, path);
        HttpURLConnection con = (HttpURLConnection) ep.openConnection();
        credential.authorizeRequest(con);
        sendRequest(request, con);

        decorateResponseStream(con.getInputStream());

        if (con.getResponseCode()==201) {
            String location = con.getHeaderField("Location");
            if (location==null)
                throw new IllegalStateException(ep+" reported 201 but there's no location header");
            return new CloudResource(new URL(location),credential);
        }
        throw new IllegalStateException(ep+" reported success "+con.getResponseCode()+" but expecting 201");
    }

    private void sendRequest(Object request, HttpURLConnection con) throws IOException {
        con.setRequestMethod("POST");
        con.setRequestProperty("Accept", CONTENT_TYPE);
        con.setRequestProperty("Content-Type", CONTENT_TYPE_UTF8);
        con.setDoOutput(true);
        for (String t : typesOf(request.getClass())) {
            con.addRequestProperty("X-Cloud-Resource-Type",t);
        }
        dumpRequestHeaders(con);
        MAPPER.writeValue(decorateRequestStream(con.getOutputStream()),request);
        con.getOutputStream().close();

        checkError(con);
    }

    public HttpURLConnection connect() throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        credential.authorizeRequest(con);
        con.setRequestProperty("Accept", CONTENT_TYPE);
        dumpRequestHeaders(con);
        checkError(con);
        List<String> v = con.getHeaderFields().get("X-Cloud-Resource-Type");
        if (v==null)
            throw new IOException(url+" is not a cloud resource. It reported no X-Cloud-Resource-Type header");
        this.types = new HashSet<String>(v);
        String ct = con.getHeaderField("Content-Type");
        if (ct==null)
            throw new IOException(url+" is not a cloud resource. It reported no Content-Type");
        if (!ct.startsWith(CONTENT_TYPE))
            throw new IOException(url+" is not a cloud resource. It reported Content-Type="+ct+" while we expected "+CONTENT_TYPE);
        return con;
    }

    /**
     * Checks if the server reported an error, and if so throw an exception with enough information.
     *
     * We assume {@link HttpURLConnection} follows redirects, so we don't process them here.
     */
    private void checkError(HttpURLConnection con) throws IOException {
        dumpResponseHeaders(con);
        if (con.getResponseCode()/100!=2) {
            String msg = "Failed to retrieve "+url;
            InputStream er = con.getErrorStream();
            if (er!=null) {
                msg += ": "+ IOUtils.toString(er);
            }
            throw new IOException(msg);
        }
    }

    private void dumpRequestHeaders(HttpURLConnection con) {
        if (DEBUG==null)    return;

        DEBUG.println();
        DEBUG.printf("> %s %s\n",con.getRequestMethod(),con.getURL());
        for (Entry<String, List<String>> e : con.getRequestProperties().entrySet()) {
            String header = e.getKey();
            for (String v : e.getValue()) {
                DEBUG.printf("> %s: %s\n",header,v);
            }
        }
    }

    private OutputStream decorateRequestStream(final OutputStream os) {
        if (DEBUG==null)    return os;

        return new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                DEBUG.write(b);
                os.write(b);
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException {
                DEBUG.write(b, off, len);
                os.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                os.flush();
            }

            @Override
            public void close() throws IOException {
                os.close();
            }
        };
    }

    private InputStream decorateResponseStream(InputStream is) throws IOException {
        if (DEBUG==null || is==null)    return is;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IOUtils.copy(is,baos);
        DEBUG.println(baos.toString());
        return new ByteArrayInputStream(baos.toByteArray());
    }

    private void dumpResponseHeaders(HttpURLConnection con) throws IOException {
        if (DEBUG==null)    return;

        DEBUG.println();
        DEBUG.printf("< %s %s\n",con.getResponseCode(),con.getResponseMessage());
        for (Entry<String, List<String>> e : con.getHeaderFields().entrySet()) {
            String header = e.getKey();
            if (header==null)   continue;   // a bug in HttpURLConnection?
            for (String v : e.getValue()) {
                DEBUG.printf("< %s: %s\n",header,v);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CloudResource that = (CloudResource) o;

        return url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    @Override
    public String toString() {
        return url.toString();
    }

    public static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(Feature.AUTO_DETECT_FIELDS, false);
        MAPPER.configure(Feature.AUTO_DETECT_SETTERS, false);
        MAPPER.registerModule(new CloudResourceJacksonModule());
    }

    /**
     * MIME content type of the cloud resource.
     */
    public static final String CONTENT_TYPE = "application/vnd.cloudbees.resource+json";
    public static final String CONTENT_TYPE_UTF8 = CONTENT_TYPE + ";charset=UTF-8";

    /**
     * Recursively list up all the {@link CloudResourceType} annotations on the given class
     * and return them.
     */
    public static Set<String> typesOf(Class<?> bean) {
        Set<String> r = new HashSet<String>();
        typesOf(bean,r);
        return r;
    }

    private static void typesOf(Class<?> t, Set<String> r) {
        if (t==null)    return;
        CloudResourceType a = t.getAnnotation(CloudResourceType.class);
        if (a!=null)
            r.add(a.value());
        for (Class<?> i : t.getInterfaces()) {
            typesOf(i,r);
        }
        typesOf(t.getSuperclass(),r);
    }

    /**
     * Capability required to retrieve the state of {@link CloudResource}.
     */
    public static Capability READ_CAPABILITY = new Capability("https://types.cloudbees.com/resource/read");

    /**
     * If set to non-null, the request and the response details are sent to this stream
     */
    public static PrintStream DEBUG = null;
}
