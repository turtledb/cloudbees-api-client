package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.util.Map;

/**
 * @author Fabian Donze
 */
@XStreamAlias("AccountRegionInfo")
public class AccountRegionInfo {
    private String name;
    private Map<String, String> settings;

    public AccountRegionInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }
}