/*
 * Copyright 2009 the original author or authors.
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

import org.gradle.api.InvalidUserDataException;
import org.gradle.api.JavaVersion;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.internal.file.FileTreeInternal;
import org.gradle.api.internal.file.temp.TemporaryFileProvider;
import org.gradle.api.internal.tasks.compile.*;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.*;
import org.gradle.api.tasks.compile.AbstractCompile;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.internal.buildoption.FeatureFlags;
import org.gradle.internal.file.Deleter;
//import org.gradle.internal.instrumentation.api.annotations.ToBeReplacedByLazyProperty;
import org.gradle.jvm.toolchain.JavaInstallationMetadata;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.gradle.language.base.internal.compile.Compiler;
import org.gradle.work.Incremental;
import org.gradle.work.InputChanges;
import org.symade.kiev.gradle.api.plugins.KievPlugin;
import org.symade.kiev.gradle.internal.tasks.compile.*;

import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Compiles Kiev source files, and optionally, Java source files.
 */
@CacheableTask
public abstract class KievCompile extends AbstractCompile implements HasCompileOptions {
    private static final Logger LOGGER = Logging.getLogger(KievPlugin.class);
    private FileCollection kievClasspath;
    private final CompileOptions compileOptions;
    private final KievCompileOptions kievCompileOptions = getProject().getObjects().newInstance(KievCompileOptions.class);
    private final FileCollection stableSources = getProject().files((Callable<FileTree>) this::getSource);
    private final Property<JavaLauncher> javaLauncher;
    private File previousCompilationDataFile;

    public KievCompile() {
        ObjectFactory objectFactory = getObjectFactory();
        CompileOptions compileOptions = objectFactory.newInstance(CompileOptions.class);
        compileOptions.setIncremental(false);
        compileOptions.getIncrementalAfterFailure().convention(true);
        this.compileOptions = compileOptions;

        JavaToolchainService javaToolchainService = getJavaToolchainService();
        this.javaLauncher = objectFactory.property(JavaLauncher.class).convention(javaToolchainService.launcherFor(it -> {}));

        CompilerForkUtils.doNotCacheIfForkingViaExecutable(compileOptions, getOutputs());
    }

    @Override
    @CompileClasspath
    @Incremental
    //@ToBeReplacedByLazyProperty
    public FileCollection getClasspath() {
        return super.getClasspath();
    }

    @TaskAction
    protected void compile(InputChanges inputChanges) {
        checkKievClasspathIsNonEmpty();
        KievJavaJointCompileSpec spec = createSpec();
        maybeDisableIncrementalCompilationAfterFailure(spec);
        WorkResult result = createCompiler(spec, inputChanges).execute(spec);
        setDidWork(result.getDidWork());
    }

    private void maybeDisableIncrementalCompilationAfterFailure(KievJavaJointCompileSpec spec) {
        if (CommandLineJavaCompileSpec.class.isAssignableFrom(spec.getClass())) {
            spec.getCompileOptions().setSupportsIncrementalCompilationAfterFailure(false);
        }
    }

    /**
     * The previous compilation analysis. Internal use only.
     *
     * @since 7.1
     */
    @OutputFile
    protected File getPreviousCompilationData() {
        if (previousCompilationDataFile == null) {
            previousCompilationDataFile = new File(getTemporaryDirWithoutCreating(), "previous-compilation-data.bin");
        }
        return previousCompilationDataFile;
    }

    private Compiler<KievJavaJointCompileSpec> createCompiler(KievJavaJointCompileSpec spec, InputChanges inputChanges) {

        //KievCompilerFactory kievCompilerFactory = getKievCompilerFactory();
        //Compiler<KievJavaJointCompileSpec> delegatingCompiler = kievCompilerFactory.newCompiler(spec);
        Compiler<KievJavaJointCompileSpec> delegatingCompiler = new InProcessKievCompiler();
        CleaningJavaCompiler<KievJavaJointCompileSpec> cleaningKievCompiler = new CleaningJavaCompiler<>(delegatingCompiler, getOutputs(), getDeleter());
        return cleaningKievCompiler;
    }

    /**
     * The sources for incremental change detection.
     *
     * @since 5.6
     */
    @SkipWhenEmpty
    @IgnoreEmptyDirectories
    // Java source files are supported, too. Therefore, we should care about the relative path.
    @PathSensitive(PathSensitivity.RELATIVE)
    @InputFiles
    protected FileCollection getStableSources() {
        return stableSources;
    }

    private FileCollection determineKievCompileClasspath() {
        return getClasspath();
    }

    private static void validateIncrementalCompilationOptions(List<File> sourceRoots, boolean annotationProcessingConfigured) {
        if (sourceRoots.isEmpty()) {
            throw new InvalidUserDataException("Unable to infer source roots. Incremental Kiev compilation requires the source roots. Change the configuration of your sources or disable incremental Kiev compilation.");
        }

        if (annotationProcessingConfigured) {
            throw new InvalidUserDataException("Enabling incremental compilation and configuring Java annotation processors for Kiev compilation is not allowed. Disable incremental Kiev compilation or remove the Java annotation processor configuration.");
        }
    }

    private List<File> copyOf(Iterable<File> iterable) {
        ArrayList<File> list = new ArrayList<>();
        for (File f : determineKievCompileClasspath())
            list.add(f);
        return list;
    }

    private KievJavaJointCompileSpec createSpec() {
        DefaultKievJavaJointCompileSpec spec = new DefaultKievJavaJointCompileSpecFactory(compileOptions, getToolchain()).create();
        assert spec != null;

        FileTreeInternal stableSourcesAsFileTree = (FileTreeInternal) getStableSources().getAsFileTree();
        List<File> sourceRoots = CompilationSourceDirs.inferSourceRoots(stableSourcesAsFileTree);

        spec.setSourcesRoots(sourceRoots);
        spec.setSourceFiles(stableSourcesAsFileTree);
        spec.setDestinationDir(getDestinationDirectory().getAsFile().get());
        spec.setWorkingDir(getProjectLayout().getProjectDirectory().getAsFile());
        spec.setTempDir(getTemporaryDir());
        spec.setCompileClasspath(copyOf(determineKievCompileClasspath()));
        configureCompatibilityOptions(spec);
        //spec.setAnnotationProcessorPath(Lists.newArrayList(compileOptions.getAnnotationProcessorPath() == null ? getProjectLayout().files() : compileOptions.getAnnotationProcessorPath()));
        spec.setKievClasspath(copyOf(getKievClasspath()));
        LOGGER.info("createSpec(): kievClasspath="+spec.getKievClasspath());
        spec.setCompileOptions(compileOptions);
        spec.setKievCompileOptions(new MinimalKievCompileOptions(kievCompileOptions));
        spec.getCompileOptions().setSupportsCompilerApi(true);
        if (getOptions().isIncremental()) {
            validateIncrementalCompilationOptions(sourceRoots, spec.annotationProcessingConfigured());
            spec.getCompileOptions().setPreviousCompilationDataFile(getPreviousCompilationData());
        }

        String executable = getJavaLauncher().get().getExecutablePath().getAsFile().getAbsolutePath();
        spec.getCompileOptions().getForkOptions().setExecutable(executable);

        return spec;
    }

    private void configureCompatibilityOptions(DefaultKievJavaJointCompileSpec spec) {
        String toolchainVersion = JavaVersion.toVersion(getToolchain().getLanguageVersion().asInt()).toString();
        String sourceCompatibility = getSourceCompatibility();
        // Compatibility can be null if no convention was configured, e.g. when JavaBasePlugin is not applied
        if (sourceCompatibility == null) {
            sourceCompatibility = toolchainVersion;
        }
        String targetCompatibility = getTargetCompatibility();
        if (targetCompatibility == null) {
            targetCompatibility = sourceCompatibility;
        }

        spec.setSourceCompatibility(sourceCompatibility);
        spec.setTargetCompatibility(targetCompatibility);
    }

    private JavaInstallationMetadata getToolchain() {
        return javaLauncher.map(JavaLauncher::getMetadata).get();
    }

    private void checkKievClasspathIsNonEmpty() {
        if (getKievClasspath().isEmpty()) {
            throw new InvalidUserDataException("'" + getName() + ".kievClasspath' must not be empty. If a Kiev compile dependency is provided, "
                + "the 'kiev-base' plugin will attempt to configure 'kievClasspath' automatically. Alternatively, you may configure 'kievClasspath' explicitly.");
        }
    }

    /**
     * We need to track the Java version of the JVM the Kiev compiler is running on, since the Kiev compiler produces different results depending on it.
     *
     * This should be replaced by a property on the Kiev toolchain as soon as we model these.
     *
     * @since 4.0
     */
    @Input
    protected String getKievCompilerJvmVersion() {
        return getToolchain().getLanguageVersion().toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Internal("tracked via stableSources")
    //@ToBeReplacedByLazyProperty
    public FileTree getSource() {
        return super.getSource();
    }

    /**
     * Gets the options for the Kiev compilation. To set specific options for the nested Java compilation, use {@link
     * #getOptions()}.
     *
     * @return The Kiev compile options. Never returns null.
     */
    @Nested
    public KievCompileOptions getKievOptions() {
        return kievCompileOptions;
    }

    /**
     * Returns the options for Java compilation.
     *
     * @return The Java compile options. Never returns null.
     */
    @Nested
    @Override
    public CompileOptions getOptions() {
        return compileOptions;
    }

    /**
     * Returns the classpath containing the version of Kiev to use for compilation.
     *
     * @return The classpath.
     */
    @Incremental
    @Classpath
    public FileCollection getKievClasspath() {
        return kievClasspath;
    }

    /**
     * Sets the classpath containing the version of Kiev to use for compilation.
     *
     * @param kievClasspath The classpath. Must not be null.
     */
    public void setKievClasspath(FileCollection kievClasspath) {
        this.kievClasspath = kievClasspath;
    }

    /**
     * The toolchain {@link JavaLauncher} to use for executing the Kiev compiler.
     *
     * @return the java launcher property
     * @since 6.8
     */
    @Nested
    public Property<JavaLauncher> getJavaLauncher() {
        return javaLauncher;
    }

    @Inject
    protected abstract Deleter getDeleter();

    @Inject
    protected abstract ProjectLayout getProjectLayout();

    @Inject
    protected abstract ObjectFactory getObjectFactory();

    @Inject
    protected abstract KievCompilerFactory getKievCompilerFactory();

    @Inject
    protected abstract FeatureFlags getFeatureFlags();

    @Inject
    protected abstract JavaToolchainService getJavaToolchainService();

    private File getTemporaryDirWithoutCreating() {
        // Do not create the temporary folder, since that causes problems.
        return getServices().get(TemporaryFileProvider.class).newTemporaryFile(getName());
    }
}
