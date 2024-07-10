/*
 * Copyright 2021 the original author or authors.
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

package org.symade.kiev.gradle.internal.tasks.compile;

import org.symade.kiev.gradle.api.tasks.compile.KievCompileOptions;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Serializable;
import java.util.*;

public class MinimalKievCompileOptions implements Serializable {
    private boolean failOnError;
    private boolean verbose;
    private boolean listFiles;
    private String encoding;
    private List<String> fileExtensions;
    private Map<String, Boolean> optimizationOptions;
    private File stubDir;
    private boolean parameters;
    private Set<String> disabledGlobalASTTransformations;

    public MinimalKievCompileOptions(KievCompileOptions compileOptions) {
        this.failOnError = compileOptions.isFailOnError();
        this.verbose = compileOptions.isVerbose();
        this.listFiles = compileOptions.isListFiles();
        this.encoding = compileOptions.getEncoding();
        this.fileExtensions = new ArrayList<>(compileOptions.getFileExtensions());
        this.optimizationOptions = new HashMap<>();
        Map<String, Boolean> options = compileOptions.getOptimizationOptions();
        if (options != null)
            this.optimizationOptions.putAll(options);
        this.stubDir = compileOptions.getStubDir();
        this.parameters = compileOptions.isParameters();
        this.disabledGlobalASTTransformations = compileOptions.getDisabledGlobalASTTransformations().get();
    }

    public boolean isFailOnError() {
        return failOnError;
    }

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isListFiles() {
        return listFiles;
    }

    public void setListFiles(boolean listFiles) {
        this.listFiles = listFiles;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public List<String> getFileExtensions() {
        return fileExtensions;
    }

    public void setFileExtensions(List<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

    @Nullable
    public Map<String, Boolean> getOptimizationOptions() {
        return optimizationOptions;
    }

    public void setOptimizationOptions(@Nullable Map<String, Boolean> optimizationOptions) {
        this.optimizationOptions = optimizationOptions;
    }

    public File getStubDir() {
        return stubDir;
    }

    public void setStubDir(File stubDir) {
        this.stubDir = stubDir;
    }

    public boolean isParameters() {
        return parameters;
    }

    public void setParameters(boolean parameters) {
        this.parameters = parameters;
    }

    public Set<String> getDisabledGlobalASTTransformations() {
        return disabledGlobalASTTransformations;
    }

    public void setDisabledGlobalASTTransformations(Set<String> disabledGlobalASTTransformations) {
        this.disabledGlobalASTTransformations = disabledGlobalASTTransformations;
    }
}
