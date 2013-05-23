package com.cloudbees.api.cr;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

import java.io.IOException;

/**
 * @author Kohsuke Kawaguchi
 */
class CloudResourceSerializer extends JsonSerializer<CloudResource> {
    @Override
    public Class<CloudResource> handledType() {
        return CloudResource.class;
    }

    @Override
    public void serialize(CloudResource value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(value.getUrl().toExternalForm());
    }
}
