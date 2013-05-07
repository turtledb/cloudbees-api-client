package com.cloudbees.oauth;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * @author Vivek Pandey
 */
@Provider
@Produces("application/json")
public class JacksonConfigurator implements ContextResolver<ObjectMapper> {

    private static final ObjectMapper mapper = createMapper();

    public ObjectMapper getContext(Class<?> arg0) {
        return mapper;
    }

    public static ObjectMapper getMapper(){
        return mapper;
    }

    private static ObjectMapper createMapper(){
        ObjectMapper mapper = new ObjectMapper();
        SerializationConfig serializationConfig = mapper.getSerializationConfig();

        //don't write null values
        serializationConfig.withSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        DeserializationConfig deserializationConfig = mapper.getDeserializationConfig();
        deserializationConfig.without(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        return mapper;
    }

}
