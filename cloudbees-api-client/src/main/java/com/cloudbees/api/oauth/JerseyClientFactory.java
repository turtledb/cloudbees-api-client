package com.cloudbees.api.oauth;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Vivek Pandey
 */
class JerseyClientFactory {
    private static final Client client=createClient();

    public static Client getClient(){
        return  client;
    }

    private static Client createClient(){
        ClientConfig cc = new DefaultClientConfig(JacksonConfigurator.class);
        cc.getClasses().add(JacksonJsonProvider.class);
        Client c = Client.create(cc);
        c.setFollowRedirects(true);
        return c;
    }

    private static final Logger logger = LoggerFactory.getLogger(JerseyClientFactory.class);
}
