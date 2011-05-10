package com.cloudbees.api;

import java.util.ArrayList;
import java.util.List;

public class ApplicationConfiguration {

	private String applicationId;
	
	private String defaultEnvironment;
	
	private List<String> appliedEnvironments = new ArrayList<String>();
	
	public ApplicationConfiguration() {
		
	}
	public String getApplicationId() {
		return applicationId;
	}
	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}
	public String getDefaultEnvironment() {
		return defaultEnvironment;
	}
	public void setDefaultEnvironment(String defaultEnvironment) {
		this.defaultEnvironment = defaultEnvironment;
	}
	public List<String> getAppliedEnvironments() {
		return appliedEnvironments;
	}
	public void setAppliedEnvironments(List<String> appliedEnvironments) {
		this.appliedEnvironments = appliedEnvironments;
	}

    @Override
    public String toString() {
        return "ApplicationConfiguration{" +
                "applicationId='" + applicationId + '\'' +
                ", defaultEnvironment='" + defaultEnvironment + '\'' +
                ", appliedEnvironments=" + appliedEnvironments +
                '}';
    }
}
