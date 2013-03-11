package com.cloudbees.api.config;

import java.lang.*;import java.lang.Boolean;import java.lang.Integer;import java.lang.NumberFormatException;import java.lang.String;import java.util.AbstractMap;

/**
 * String-to-string map, with lots of type converters for values and other convenience methods.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class ParameterMap extends AbstractMap<String,String> {

    public int getInt(String key, int defaultValue) throws NumberFormatException {
        String v = get(key);
        if (v==null)    return defaultValue;
        return Integer.parseInt(v);
    }

    public void putInt(String key, int value) {
        put(key,String.valueOf(value));
    }

    /**
     * Gets the configuration value as a boolean.
     *
     * The expected string representation is "true" or "false". Anything else will be treated as "false".
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String v = get(key);
        if (v==null)    return defaultValue;
        return Boolean.parseBoolean(v);
    }

    public void putBoolean(String key, boolean value) {
        put(key,String.valueOf(value));
    }

    public double getDouble(String key, double defaultValue) throws NumberFormatException {
        String v = get(key);
        if (v==null)    return defaultValue;
        return java.lang.Double.parseDouble(v);
    }

    public void putDouble(String key, double value) {
        put(key,String.valueOf(value));
    }

    // TODO: add more
}
