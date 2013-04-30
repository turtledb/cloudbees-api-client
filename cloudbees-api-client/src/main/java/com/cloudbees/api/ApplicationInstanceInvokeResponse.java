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

@XStreamAlias("ApplicationInstanceInvokeResponse")
public class ApplicationInstanceInvokeResponse {
    private String out;
    private String exitCode;

    public ApplicationInstanceInvokeResponse() {
    }

    public ApplicationInstanceInvokeResponse(String out, String exitCode) {
        this.out = out;
        this.exitCode = exitCode;
    }

    public void setOut(String out) {
        this.out = out;
    }

    public String getOut() {
        return out;
    }

    public String getExitCode() {
        return exitCode;
    }

    public void setExitCode(String exitCode) {
        this.exitCode = exitCode;
    }
}