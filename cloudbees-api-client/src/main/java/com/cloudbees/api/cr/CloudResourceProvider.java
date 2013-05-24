package com.cloudbees.api.cr;

import org.codehaus.jackson.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Lists up {@link CloudResource}s that belong to it.
 *
 * @author Kohsuke Kawaguchi
 */
@CloudResourceType("https://types.cloudbees.com/resource/provider")
public class CloudResourceProvider extends CloudResourceFacet implements Iterable<CloudResource> {
    public CloudResourceProvider(CloudResource owner) {
        super(owner);
    }

    /**
     * Lists up all the cloud resources.
     */
    public Iterator<CloudResource> iterator() {
        try {
            JsonNode res = getOwner().retrieve().get("resources");
            if (!res.isArray())
                throw new IllegalStateException("Expected a JSON array but "+getOwner()+" gave us "+res);
            ReferencedResource[] dto = CloudResource.MAPPER.readValue(res, ReferencedResource[].class);
            List<CloudResource> r = new ArrayList<CloudResource>(dto.length);
            for (ReferencedResource rr : dto) {
                r.add(rr.toCloudResource(getOwner()));
            }

            return r.iterator();
        } catch (IOException e) {
            throw new Error(e);
        }
    }
}
