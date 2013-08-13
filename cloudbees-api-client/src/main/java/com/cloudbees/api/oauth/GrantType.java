package com.cloudbees.api.oauth;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.IOException;
import java.util.Locale;

/**
 * Possible modes of token creation that applications are allowed to do.
 *
 * @author Kohsuke Kawaguchi
 * @see OauthClientApplication#grant_types
 */
@JsonSerialize(using=GrantType.SerializerImpl.class)
@JsonDeserialize(using=GrantType.DeserializerImpl.class)
public enum GrantType {
    /**
     * This is for a stereo-typical OAuth flow between a web application  that users access via a browser.
     * The web application redirects the user browser to Grand Central, then GC performs authentication,
     * and the user is sent back to the web application with authorization code.
     *
     * The web application can then pass the authorization code to GC to get the token.
     */
    AUTHORIZATION_CODE,

    /**
     * This mode of token issuance is to use the OAuth application client ID and secret directly to
     * create a token.
     */
    CLIENT_CREDENTIALS;

    private final String lowerCase;

    public String toLowerCase() { return lowerCase; }

    private GrantType() {
        lowerCase = name().toLowerCase(Locale.ENGLISH);
    }

    /**
     * From the lower/upper case of the grant type value, obtains the enum constant.
     */
    public static GrantType parse(String value) {
        for (GrantType gt : GrantType.values()) {
            if (gt.name().equalsIgnoreCase(value))
                return gt;
        }
        return null;
    }

    public static class SerializerImpl extends JsonSerializer<GrantType> {
        @Override
        public void serialize(GrantType o, JsonGenerator gen, SerializerProvider serializer) throws IOException {
            gen.writeString(o.toLowerCase());
        }
    }
    public static class DeserializerImpl extends JsonDeserializer<GrantType> {
        @Override
        public GrantType deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            return parse(jp.getText());
        }
    }
}
