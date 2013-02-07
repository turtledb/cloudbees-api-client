package com.cloudbees.oauth;


import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Vivek Pandey
 */
public class JerseyClientFactory {
    private static final Client client=createClient();

    public static Client getClient(){
        return  client;
    }

    private static Client createClient(){
        ClientConfig cc = new DefaultClientConfig(JacksonConfigurator.class);

        //Bypass invalid SSL certificate
        try {
            TrustManager[] trustAllCerts =
                    new TrustManager[]{new X509TrustManager() {

                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                java.security.cert.X509Certificate[] certs,
                                String authType) {
                            // No need to implement.
                        }

                        public void checkServerTrusted(
                                java.security.cert.X509Certificate[] certs,
                                String authType) {
                            // No need to implement.
                        }
                    }};

            HostnameVerifier nullHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HTTPSProperties httpsProps = new HTTPSProperties(new HostnameVerifier() {
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            }, sc);

            cc.getProperties().put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, httpsProps);


        } catch (NoSuchAlgorithmException e) {
            logger.error("Stupid SSL certificates!", e);
        } catch (KeyManagementException e) {
            logger.error(e.getMessage(), e);
        }

        cc.getClasses().add(JacksonJsonProvider.class);
        Client c = Client.create(cc);
        c.setFollowRedirects(true);
        return c;
    }

    private static final Logger logger = LoggerFactory.getLogger(JerseyClientFactory.class);
}
