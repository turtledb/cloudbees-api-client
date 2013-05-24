/*
 * Copyright 2010-2012, CloudBees Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.cloudbees.api;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * @author Fabian Donze
 */
@XStreamAlias("ApplicationSnapshotInfo")
public class ApplicationSnapshotInfo {
    private String id;
    private String applicationId;
    private Map<String, String> settings;

    /**
     * @deprecated use settings.get("created")
     */
    private Date created;

    public ApplicationSnapshotInfo() {
    }

    public ApplicationSnapshotInfo(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }
    
    public Date getCreated() {
        if (created != null) {
            return created;
        }
        try {
            Date createDate =  null;
            String created = settings.get("created");
            if(created != null) {
                createDate = DateHelper.parseW3CDate(settings.get("created"));
            }
            return createDate;
        } catch (ParseException e) {
            return null;
        }
    }
    
    public String getTitle() {
        return settings.get("title");
    }
}