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

import org.apache.commons.httpclient.HttpMethod;

import java.io.IOException;

/**
 * @author Fabian Donze
 */
public class HttpReply {
    private final HttpMethod method;
    private final int code;
    private final String content;

    public HttpReply(int code, String content) {
        this(null,code,content);
    }

    public HttpReply(HttpMethod method, int code, String content) {
        this.method = method;
        this.code = code;
        this.content = content;
    }

    public int getCode() {
        return code;
    }

    public String getContent() {
        return content;
    }

    /**
     * Treats the response as JSON string and performs a databinding.
     */
    public <T> T bind(Class<T> type, BeesClient root) throws IOException {
        if (getCode() >= 300)
            throw new IOException("Failed to " + method.getName() + " : " + method.getURI() + " : code=" + getCode() + " response=" + getContent());

        if (type != null && getContent() != null) {
            T ret = BeesClient.MAPPER.readValue(getContent(), type);
            if (ret instanceof CBObject) {// TODO: nested objects?
                ((CBObject) ret).root = root;
            }
            return ret;
        }
        return null;
    }

    @Override
    public String toString() {
        return "HttpReply{" +
                "code=" + code +
                ", content='" + content + '\'' +
                '}';
    }
    public String toShortString(int max) {
        String str;
        if (content.length() > max)
            str = content.substring(0, max) + "...";
        else
            str = content;
        return "Reply{" +
                "code=" + code +
                ", content='" + str + '\'' +
                '}';
    }
}
