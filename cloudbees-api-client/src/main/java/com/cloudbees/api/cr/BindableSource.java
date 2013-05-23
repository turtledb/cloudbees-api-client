package com.cloudbees.api.cr;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
@CloudResourceType("https://types.cloudbees.com/binding/source")
public class BindableSource extends CloudResourceFacet {
    public BindableSource(CloudResource owner) {
        super(owner);
    }

    /**
     * Gets a binding collection object
     */
    public BindingCollection getBindingCollection() throws IOException {
        return new CloudResource(new URL(owner.retrieve().get("bindingCollection").asText()), owner.getCredential()).coerce(BindingCollection.class);
    }
}