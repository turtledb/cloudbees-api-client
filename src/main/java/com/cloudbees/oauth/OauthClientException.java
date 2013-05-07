package com.cloudbees.oauth;

/**
 * @author Vivek Pandey
 */
public class OauthClientException extends Exception{

    public OauthClientException(String message) {
        super(message);
    }

    public OauthClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public OauthClientException(Throwable cause) {
        super(cause);
    }
}
