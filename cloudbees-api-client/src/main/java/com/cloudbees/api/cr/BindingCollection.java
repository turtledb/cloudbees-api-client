package com.cloudbees.api.cr;

import org.codehaus.jackson.annotate.JsonProperty;

import java.io.IOException;
import java.util.Map;

/**
 * @author Kohsuke Kawaguchi
 */
@CloudResourceType("https://types.cloudbees.com/binding/collection")
public class BindingCollection extends CloudResourceFacet {
    public BindingCollection(CloudResource owner) {
        super(owner);
    }

    /**
     * Bind the {@link BindableSource} (that this collection serves) to the specified sink.
     *
     * @return
     *      Newly created edge.
     */
    public CloudResource bind(CloudResource sink, String label, Map<String,String> settings) throws IOException {
        BindRequest br = new BindRequest();
        br.sink = sink;
        br.label = label;
        br.settings = settings;
        return getOwner().create("./bindings",br);
    }

    @CloudResourceType("https://types.cloudbees.com/binding/edge")
    static class BindRequest {
        @JsonProperty("sink")
        public CloudResource sink;
        @JsonProperty("label")
        public String label;
        @JsonProperty("settings")
        public Map<String,String> settings;
    }

    /**
     * Capability needed to call {@link #bind(CloudResource, String, Map)}
     */
    public static Capability BIND_CAPABILITY = new Capability("https://types.cloudbees.com/binding/bind");
}
