package com.cloudbees.api.cr;

import org.codehaus.jackson.annotate.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * JSON data-bound bean for a referenced cloud resource.
 *
 * @author Kohsuke Kawaguchi
 */
public class ReferencedResource {
    @JsonProperty
    public String url;

    @JsonProperty
    public List<String> types;

    public CloudResource toCloudResource(CloudResource context) throws MalformedURLException {
        CloudResource cr = new CloudResource(new URL(url), context.getCredential());
        cr.setTypes(types);
        return cr;
    }
}