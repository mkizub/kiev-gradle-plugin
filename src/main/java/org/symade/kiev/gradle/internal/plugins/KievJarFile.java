/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.symade.kiev.gradle.internal.plugins;

import org.gradle.util.internal.VersionNumber;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KievJarFile {
    private static final Pattern FILE_NAME_PATTERN = Pattern.compile("(symade(?:-all)?)-(\\d.*|core).jar");

    private final File file;
    private final Matcher matcher;

    private KievJarFile(File file, Matcher matcher) {
        this.file = file;
        this.matcher = matcher;
    }

    public File getFile() {
        return file;
    }

    public String getBaseName() {
        return matcher.group(1);
    }

    public VersionNumber getVersion() {
        String version = matcher.group(2);
        if (version.equals("core"))
            return new VersionNumber(0, 6, 0, "SNAPSHOT");
        return VersionNumber.parse(matcher.group(2));
    }

    public boolean isKievAll() {
        return getBaseName().equals("symade-all");
    }

    @Nullable
    public static KievJarFile parse(File file) {
        try {
            if (file.getName().contains("symade")) {
                // Resolve a symlink file to the real location
                file = file.toPath().toRealPath().toFile();
            }
        } catch (IOException e) {
            // Let the code use the original File otherwise
        }
        Matcher matcher = FILE_NAME_PATTERN.matcher(file.getName());
        return matcher.matches() ? new KievJarFile(file, matcher) : null;
    }
}
