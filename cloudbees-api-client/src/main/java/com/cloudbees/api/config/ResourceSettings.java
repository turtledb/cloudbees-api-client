package com.cloudbees.api.config;

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;import java.lang.Object;import java.lang.Override;import java.lang.String;

/**
 * @author Fabian Donze
 */
public class ResourceSettings {
	@XStreamAsAttribute
	private String name;
    @XStreamAsAttribute
    private String value;
	@XStreamAsAttribute
	private String type;
    @XStreamAsAttribute
    private String scope;
    @XStreamAsAttribute
    private String auth;
    @XStreamAsAttribute
    private String delim;

    @XStreamImplicit(itemFieldName="param")
    private ParameterList<ParameterSettings> parameters = new ParameterList<ParameterSettings>();

    public ResourceSettings(String name, String type) {
        this.name = name;
        this.type = type;
    }
    public ResourceSettings(String name, String type, String value) {
        this.name = name;
        this.type = type;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type == null ? "internal" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getDelim() {
        return delim;
    }

    public void setDelim(String delim) {
        this.delim = delim;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public ParameterMap getParameters() {
        if (parameters == null)
            parameters = new ParameterList<ParameterSettings>();
        return parameters.asMap();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ResourceSettings that = (ResourceSettings) o;

        if (auth != null ? !auth.equals(that.auth) : that.auth != null) return false;
        if (delim != null ? !delim.equals(that.delim) : that.delim != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        if (scope != null ? !scope.equals(that.scope) : that.scope != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value != null ? value.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (auth != null ? auth.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (delim != null ? delim.hashCode() : 0);
        result = 31 * result + (scope != null ? scope.hashCode() : 0);
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ResourceSettings{" +
                "value='" + value + '\'' +
                ", name='" + name + '\'' +
                ", auth='" + auth + '\'' +
                ", type='" + type + '\'' +
                ", delim='" + delim + '\'' +
                ", scope='" + scope + '\'' +
                ", parameters=" + parameters +
                '}';
    }
}
