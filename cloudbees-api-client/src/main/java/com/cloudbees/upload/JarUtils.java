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

package com.cloudbees.upload;

import com.cloudbees.utils.ZipHelper;
import com.thoughtworks.xstream.XStream;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * @author Fabian Donze
 */
public final class JarUtils {

    private static final String META_INF = "META-INF";
    private static final String JAR_FILE = "CB-JAR.xml";

    private JarUtils() {
        throw new IllegalAccessError("Utility class");
    }

    public static Map<String, String> getJarHashes(File warFile) throws IOException {
        Map<String, String> hashes = new HashMap<String, String>();

        ZipFile zipFile = new ZipFile(warFile.getAbsolutePath());
        Enumeration<? extends ZipEntry> e = zipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = e.nextElement();
            String name = entry.getName();
            if (name.endsWith(".jar")) {
                hashes.put(name, sha256(zipFile.getInputStream(entry)));
            }
        }

        zipFile.close();
        return hashes;
    }

    public static File createDeltaWarFile(Map<String, String> existingArchiveJars, File warFile, String tmp)
            throws IOException {
        Map<String, String> hashes = new HashMap<String, String>();

        String tmpDir = makeTmpDir(warFile, tmp);
        ZipFile zipFile = new ZipFile(warFile.getAbsolutePath());
        Enumeration<? extends ZipEntry> e = zipFile.entries();
        while (e.hasMoreElements()) {
            ZipEntry entry = e.nextElement();
            String name = entry.getName();
            String hash = existingArchiveJars.get(name);
            String entrySha = sha256(zipFile.getInputStream(entry));
            if (hash != null && hash.equals(entrySha)) {
                hashes.put(name, entrySha);
            } else {
                // Copy the entry to tmp directory
                unArchiveZipEntry(tmpDir, zipFile, entry);
            }
        }
        zipFile.close();

        // Add a file to the archive with the CRCs
        File metaInfDir = new File(tmpDir + META_INF);
        metaInfDir.mkdirs();

        File deltaFile = new File(metaInfDir, JAR_FILE);
        XStream xstream = new XStream();
        FileOutputStream fos = new FileOutputStream(deltaFile);
        xstream.toXML(hashes, fos);
        fos.close();

        // Archive the deltas
        String deltaDir = warFile.getParent() == null ? "." : warFile.getParent();
        String deltaArchiveFile = deltaDir + "/JAR-" + warFile.getName();
        archiveDirectory(tmpDir, deltaArchiveFile);

        deleteAll(new File(tmpDir));

        return new File(deltaArchiveFile);
    }

    private static void unArchiveZipEntry(String destinationDirectory, ZipFile zipfile, ZipEntry entry)
            throws IOException {
        File file = ZipHelper.unzipEntryToFolder(entry, zipfile.getInputStream(entry), new File(destinationDirectory));
        if (entry.getTime() > -1) {
            file.setLastModified(entry.getTime());
        }
    }


    private static void archiveDirectory(String directory, String archiveFile) throws IOException {
        File archive = new File(archiveFile);
        archive.createNewFile();
        FileOutputStream fileOutputStream = new FileOutputStream(archive);
        ZipOutputStream zipOutputStream = new ZipOutputStream(new BufferedOutputStream(fileOutputStream));
        ZipHelper.addDirectoryToZip(new File(directory), new File(directory), null, zipOutputStream);
        zipOutputStream.close();
    }

    private static String makeTmpDir(File file, String tmp) {
        if (tmp == null) {
            tmp = ".";
        }
        String fileName = file.getName();
        int idx = fileName.lastIndexOf('.');
        if (idx > -1) {
            fileName = fileName.substring(0, idx);
        }
        if (!tmp.endsWith(File.separator)) {
            tmp += File.separator;
        }

        tmp = tmp + "tmp" + fileName + File.separator;
        File dir = new File(tmp);
        deleteAll(dir);
        dir.mkdirs();
        return tmp;
    }

    private static void deleteAll(File dir) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        if (f.isDirectory()) {
                            deleteAll(f);
                        } else {
                            f.delete();
                        }
                    }
                }
            }
            dir.delete();
        }
    }

    private static final int STREAM_BUFFER_LENGTH = 1024;

    static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static String sha256(InputStream data) throws IOException {
        MessageDigest digest = getDigest("SHA-256");
        byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
        int read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);

        while (read > -1) {
            digest.update(buffer, 0, read);
            read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);
        }

        return asHex(digest.digest());
    }

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    private static String asHex(byte[] buf) {
        char[] chars = new char[2 * buf.length];
        for (int i = 0; i < buf.length; ++i) {
            chars[2 * i] = HEX_CHARS[(buf[i] & 0xF0) >>> 4];
            chars[2 * i + 1] = HEX_CHARS[buf[i] & 0x0F];
        }
        return new String(chars);
    }
}
