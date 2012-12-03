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

import java.io.PrintStream;

public class HashWriteProgress implements UploadProgress {
    private final PrintStream out;
    boolean uploadComplete = false;
    long hashMarkCount = 0;

    public HashWriteProgress() {
        this(System.out);
    }

    public HashWriteProgress(PrintStream out) {
        this.out = out;
    }

    public void handleBytesWritten(long deltaCount,
                                   long totalWritten, long totalToSend) {
        if (uploadComplete) {
            return;
        }

        int totalMarks = (int) (totalWritten / (totalToSend / 100f));
        while (hashMarkCount < totalMarks) {
            hashMarkCount++;
            if (hashMarkCount % 25 == 0) {
                if (hashMarkCount < 100) {
                    out.println(String.format("uploaded %d%%", hashMarkCount));
                } else {
                    //upload completed (or will very soon)
                    uploadComplete = true;
                    out.println("upload completed");
                    out.println("deploying application to server(s)...");
                }
            } else {
                out.print(".");
            }
        }
    }
}
