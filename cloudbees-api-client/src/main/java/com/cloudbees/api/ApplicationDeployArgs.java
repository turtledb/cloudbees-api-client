package com.cloudbees.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ApplicationDeployArgs {
    public final String appId;
    public final boolean create;
    public final String environment;
    public final String description;
    public final String archiveFile;

    public final String srcFile;
    public final String archiveType;
    public final boolean deltaDeploy;
    public final Map<String, String> parameters;
    public final Map<String, String> variables;
    public final UploadProgress progress;

    private ApplicationDeployArgs(Builder b) {
        appId = b.appId;
        create = b.create;
        environment = b.environment;
        description = b.description;
        archiveFile = b.archiveFile;
        srcFile = b.srcFile;
        deltaDeploy = b.deltaDeploy;
        archiveType = b.archiveType;
        parameters = Collections.unmodifiableMap(b.parameters);
        variables = Collections.unmodifiableMap(b.variables);
        progress = b.progress;
    }

    public static class Builder {
        String appId;
        boolean create = false;
        String environment;
        String description;
        String archiveFile;
        String srcFile;
        String archiveType;
        boolean deltaDeploy = true;
        Map<String, String> parameters = new HashMap<String, String>();
        Map<String, String> variables = new HashMap<String, String>();
        UploadProgress progress;

        public Builder(String appId) {
            this.appId = appId;
        }

        public String getAppId() {
            return appId;
        }

        public Builder appId(String appId) {
            this.appId = appId;
            return this;
        }

        public Builder asNewApp() {
            this.create = true;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        public Builder warFile(String file) {
            return deployPackage(file, "war");
        }
        
        public Builder earFile(String file) {
            return deployPackage(file, "ear");
        }

        public Builder deployPackage(String file, String type) {
            this.archiveType = type;
            this.archiveFile = file;
            return this;
        }

        public Builder srcFile(String srcFile) {
            this.srcFile = srcFile;
            return this;
        }

        public Builder archiveType(String archiveType) {
            this.archiveType = archiveType;
            return this;
        }

        public Builder incrementalDeployment(boolean deltaDeploy) {
            this.deltaDeploy = deltaDeploy;
            return this;
        }

        public Builder withIncrementalDeployment() {
            this.deltaDeploy = true;
            return this;
        }

        public Builder withParam(String name, String value) {
            parameters.put(name, value);
            return this;
        }

        public Builder withParams(Map<String, String> params) {
            if (params != null)
                parameters.putAll(params);
            return this;
        }

        public Builder withVar(String name, String value) {
            variables.put(name, value);
            return this;
        }

        public Builder withVars(Map<String, String> vars) {
            if (vars != null)
                variables.putAll(vars);
            return this;
        }

        public Builder withProgressFeedback(UploadProgress progress) {
            this.progress = progress;
            return this;
        }

        public ApplicationDeployArgs build() {
            if (archiveType == null || archiveFile == null)
                throw new IllegalStateException("no archive was provided");
            return new ApplicationDeployArgs(this);
        }
    }

}
