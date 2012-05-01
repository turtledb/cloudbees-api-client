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

import java.util.ArrayList;
import java.util.List;

@XStreamAlias("DatabaseSnapshotListResponse")
public class DatabaseSnapshotListResponse {
    private List<DatabaseSnapshotInfo> snapshots;

    public DatabaseSnapshotListResponse() {
    }

    public List<DatabaseSnapshotInfo> getSnapshots() {
        if(snapshots == null)
            snapshots = new ArrayList<DatabaseSnapshotInfo>();
        return snapshots;
    }

    public void setSnapshots(List<DatabaseSnapshotInfo> snapshots) {
        this.snapshots = snapshots;
    }
}