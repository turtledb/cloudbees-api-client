package com.cloudbees.api.cr;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.Base64Variants;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Client-side representation of a remote cloud resource.
 *
 * @author Kohsuke Kawaguchi
 */
public class CloudResource {
    private final URL url;

    /**
     * The value to set into the Authorize header.
     */
    private final String authorization;

    private volatile List<String> types;
    private volatile ObjectNode payload;

    /**
     * A cloud resource is identified by its URL, and to talk to it we need an OAuth access token
     */
    public CloudResource(URL url, String accessToken) {
        this.url = url;
        try {
            this.authorization = "bearer "+ Base64Variants.MIME_NO_LINEFEEDS.encode(accessToken.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * URL of this cloud resource.
     */
    public URL getUrl() {
        return url;
    }

    /**
     * Creates a client-side wrapper to access this cloud resource through the specified cloud resource type.
     */
    public <T extends CloudResourceFacet> T as(Class<T> facet) throws IOException {
        if (hasType(facet))
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
        throw new ClassCastException(String.format("%s is not of type: %s. Valid types are %s", url, facet, types));
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
     * Retrieves the current state of the CloudResource object as JSON DOM node.
     *
     * @param force
     *      if false, cached value is used if available.
     */
    public ObjectNode retrieve(boolean force) throws IOException {
        if (force || payload==null) {
            HttpURLConnection con = connect();
            // TODO: we need to honor encoding in the Content-Type header, instead of letting Jackson guess it
            payload = (ObjectNode)OM.readTree(con.getInputStream());
        }
        return payload;
    }

    public ObjectNode retrieve() throws IOException {
        return retrieve(false);
    }

    /**
     * Retrieves the current state of the cloud resource and data-bind it to the given obejct via Jackson
     */
    public <T> T retrieve(Class<T> type, boolean force) throws IOException {
        return OM.readValue(retrieve(force),type);
    }

    public <T> T retrieve(Class<T> type) throws IOException {
        return retrieve(type,false);
    }

    public HttpURLConnection connect() throws IOException {
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Authorization",authorization);
        con.setRequestProperty("Accept", CONTENT_TYPE);
        if (con.getResponseCode()/100!=2) {
            String msg = "Failed to retrieve "+url;
            InputStream er = con.getErrorStream();
            if (er!=null) {
                msg += ": "+ IOUtils.toString(er);
            }
            throw new IOException(msg);
        }
        List<String> v = con.getHeaderFields().get("X-Cloud-Resource-Type");
        if (v==null)
            throw new IOException(url+" is not a cloud resource. It reported no X-Cloud-Resource-Type header");
        this.types = v;
        String ct = con.getHeaderField("Content-Type");
        if (ct==null)
            throw new IOException(url+" is not a cloud resource. It reported no Content-Type");
        if (!ct.startsWith(CONTENT_TYPE))
            throw new IOException(url+" is not a cloud resource. It reported Content-Type="+ct+" while we expected "+CONTENT_TYPE);
        return con;
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

    private static final ObjectMapper OM = new ObjectMapper();
    static {
        OM.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * MIME content type of the cloud resource.
     */
    public static final String CONTENT_TYPE = "application/vnd.cloudbees.resource+json";
}
