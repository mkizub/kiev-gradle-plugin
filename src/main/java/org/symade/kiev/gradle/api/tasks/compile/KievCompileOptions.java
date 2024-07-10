/*
 * Copyright 2007-2008 the original author or authors.
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
package org.symade.kiev.gradle.api.tasks.compile;

import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.compile.AbstractOptions;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.util.*;

/**
 * Compilation options to be passed to the Kiev compiler.
 */
public abstract class KievCompileOptions extends AbstractOptions {

    private static final long serialVersionUID = 4291388680508949776L;

    private boolean failOnError = true;

    private boolean verbose;

    private boolean listFiles;

    private String encoding = "UTF-8";

    private List<String> fileExtensions = Arrays.asList("java", "kiev");

    private Map<String, Boolean> optimizationOptions = new HashMap<>();

    private File stubDir;

    private boolean parameters;

    private final SetProperty<String> disabledGlobalASTTransformations = getObjectFactory().setProperty(String.class);

    @Inject
    protected ObjectFactory getObjectFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Tells whether the compilation task should fail if compile errors occurred. Defaults to {@code true}.
     */
    @Input
    public boolean isFailOnError() {
        return failOnError;
    }

    /**
     * Sets whether the compilation task should fail if compile errors occurred. Defaults to {@code true}.
     */
    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    /**
     * Tells whether to turn on verbose output. Defaults to {@code false}.
     */
    @Console
    public boolean isVerbose() {
        return verbose;
    }

    /**
     * Sets whether to turn on verbose output. Defaults to {@code false}.
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    /**
     * Tells whether to print which source files are to be compiled. Defaults to {@code false}.
     */
    @Console
    public boolean isListFiles() {
        return listFiles;
    }

    /**
     * Sets whether to print which source files are to be compiled. Defaults to {@code false}.
     */
    public void setListFiles(boolean listFiles) {
        this.listFiles = listFiles;
    }

    /**
     * Tells the source encoding. Defaults to {@code UTF-8}.
     */
    @Input
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets the source encoding. Defaults to {@code UTF-8}.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    /**
     * Whether the Kiev compiler generate metadata for reflection on method parameter names on JDK 8 and above.
     *
     * @since 6.1
     */
    @Input
    public boolean isParameters() {
        return parameters;
    }

    /**
     * Sets whether metadata for reflection on method parameter names should be generated.
     * Defaults to {@code false}
     *
     * @since 6.1
     */
    public void setParameters(boolean parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns optimization options for the Kiev compiler. Allowed values for an option are {@code true} and {@code false}.
     * Only takes effect when compiling against Kiev 1.8 or higher.
     *
     * <p>Known options are:
     *
     * <dl>
     *     <dt>indy
     *     <dd>Use the invokedynamic bytecode instruction. Requires JDK7 or higher and Kiev 2.0 or higher. Disabled by default.
     *     <dt>int
     *     <dd>Optimize operations on primitive types (e.g. integers). Enabled by default.
     *     <dt>all
     *     <dd>Enable or disable all optimizations. Note that some optimizations might be mutually exclusive.
     * </dl>
     */
    @Nullable @Optional @Input
    public Map<String, Boolean> getOptimizationOptions() {
        return optimizationOptions;
    }

    /**
     * Sets optimization options for the Kiev compiler. Allowed values for an option are {@code true} and {@code false}.
     * Only takes effect when compiling against Kiev 1.8 or higher.
     */
    public void setOptimizationOptions(@Nullable Map<String, Boolean> optimizationOptions) {
        this.optimizationOptions = optimizationOptions;
    }

    /**
     * Returns the set of global AST transformations which should not be loaded into the Kiev compiler.
     *
     * @see <a href="https://docs.groovy-lang.org/latest/html/api/org/codehaus/groovy/control/CompilerConfiguration.html#setDisabledGlobalASTTransformations(java.util.Set)">CompilerConfiguration</a>
     * @since 7.4
     */
    @Input
    public SetProperty<String> getDisabledGlobalASTTransformations() {
        return disabledGlobalASTTransformations;
    }

    /**
     * Returns the directory where Java stubs for Kiev classes will be stored during Java/Kiev joint
     * compilation. Defaults to {@code null}, in which case a temporary directory will be used.
     */
    @Internal
    // TOOD:LPTR Should be just a relative path
    public File getStubDir() {
        return stubDir;
    }

    /**
     * Sets the directory where Java stubs for Kiev classes will be stored during Java/Kiev joint
     * compilation. Defaults to {@code null}, in which case a temporary directory will be used.
     */
    public void setStubDir(File stubDir) {
        this.stubDir = stubDir;
    }

    /**
     * Returns the list of acceptable source file extensions. Only takes effect when compiling against
     * Kiev 1.7 or higher. Defaults to {@code ImmutableList.of("java", "kiev")}.
     */
    @Input
    public List<String> getFileExtensions() {
        return fileExtensions;
    }

    /**
     * Sets the list of acceptable source file extensions. Only takes effect when compiling against
     * Kiev 1.7 or higher. Defaults to {@code ImmutableList.of("java", "Kiev")}.
     */
    public void setFileExtensions(List<String> fileExtensions) {
        this.fileExtensions = fileExtensions;
    }

}
