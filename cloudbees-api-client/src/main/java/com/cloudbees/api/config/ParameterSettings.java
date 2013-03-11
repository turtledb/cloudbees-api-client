package com.cloudbees.api.config;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

import java.lang.Object;import java.lang.Override;import java.lang.String;import java.util.Map;

/**
 * Key/value pair, used as the persistence structure for XStream
 * but hidden from users of this package.
 *
 * @author Fabian Donze
 */
class ParameterSettings implements Map.Entry<String,String> {
    @XStreamAsAttribute
    private String name;
    @XStreamAsAttribute
    private String value;

    public ParameterSettings(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    @Override
    public String getKey() {
        return name;
    }

    @Override
    public String toString() {
        return "ParameterSettings{" +
                "name='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public String setValue(String value) {
        String old = this.value;
        this.value = value;
        return old;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ParameterSettings that = (ParameterSettings) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
