package com.cloudbees.api.cr;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
@CloudResourceType("https://types.cloudbees.com/resource/provider/crp/registry")
public class CloudResourceProviderRegistry extends CloudResourceProvider {
    public CloudResourceProviderRegistry(CloudResource owner) {
        super(owner);
    }

    /**
     * Registers a new cloud resource provider to this registry
     */
    public void register(CloudResourceProvider cr) throws IOException {
        getOwner().post(".", ReferencedResource.of(cr.getOwner()), null);
    }
}
