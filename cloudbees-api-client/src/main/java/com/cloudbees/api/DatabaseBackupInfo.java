/*
 * Copyright 2010-2013, CloudBees Inc.
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

@XStreamAlias("DatabaseBackupInfo")
public class DatabaseBackupInfo {
    private String id;
    private String created;
    private Map<String, String> settings;

    public DatabaseBackupInfo(String id, Date created) {
        super();
        this.id = id;
        this.created = DateHelper.toW3CDateString(created);
    }

    public String getId() {
        return id;
    }

    public Date getCreated() {
        if (created == null) {
            return null;
        }
        try {
            return DateHelper.parseW3CDate(created);
        } catch (ParseException e) {
            return null;
        }
    }

    public Map<String, String> getSettings() {
        return settings;
    }

    public void setSettings(Map<String, String> settings) {
        this.settings = settings;
    }
}
