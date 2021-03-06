package com.cloudbees.api.cr;

import java.net.URL;

/**
 * {@link Capability} is a portion of the OAuth scope and represents
 * the permission to invoke a specific set of operations defined by
 * cloud resource types.
 *
 * @author Kohsuke Kawaguchi
 */
public class Capability {
    private final String uri;

    public Capability(String uri) {
        this.uri = uri;
    }

    /**
     * Convenience method to create OAuth scope string representation of this capability to a cloud
     * resource at the given URL.
     */
    public String to(URL resource) {
        String base = resource.getHost();
        int p = resource.getPort();
        if (resource.getDefaultPort()!=p && p!=-1) {// -1 means not set
            base += ":"+p;
        }

        return toScope(base);
    }

    /**
     * Creates a scope string that represents this capability on any node.
     */
    public String toAll() {
        return toScope("*");
    }

    private String toScope(String domain) {
        return String.format("crs://%s/!%s",domain,uri);
    }

    public String to(CloudResource cr) {
        return to(cr.getUrl());
    }

    @Override
    public String toString() {
        return uri;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Capability that = (Capability) o;

        return uri.equals(that.uri);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }
}
