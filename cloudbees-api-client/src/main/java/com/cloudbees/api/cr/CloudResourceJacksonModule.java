package com.cloudbees.api.cr;

import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.module.SimpleModule;

/**
 * @author Kohsuke Kawaguchi
 */
public class CloudResourceJacksonModule extends SimpleModule {
    public CloudResourceJacksonModule() {
        super(CloudResourceJacksonModule.class.getName(), new Version(1,0,0,null));
        addSerializer(new CloudResourceSerializer());
    }
}
