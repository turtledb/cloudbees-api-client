package com.cloudbees.oauth;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import javax.ws.rs.Produces;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

/**
 * This is not a public interface of this library, even though for technical reasons
 * it has to be marked public. Please do not use this class.
 *
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

        mapper.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);

        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return mapper;
    }

}
