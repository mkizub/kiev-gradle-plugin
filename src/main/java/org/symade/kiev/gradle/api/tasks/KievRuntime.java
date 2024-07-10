/*
 * Copyright 2013 the original author or authors.
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
package org.symade.kiev.gradle.api.tasks;

import org.gradle.api.Buildable;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.file.collections.FailingFileCollection;
import org.gradle.api.internal.file.collections.LazilyInitializedFileCollection;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.internal.tasks.TaskDependencyResolveContext;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.jvm.internal.JvmPluginServices;
import org.gradle.util.internal.VersionNumber;
import org.symade.kiev.gradle.internal.plugins.KievJarFile;
import org.symade.kiev.gradle.api.plugins.KievPlugin;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

/**
 * Provides information related to the Kiev runtime(s) used in a project. Added by the
 * {@link org.symade.kiev.gradle.api.plugins.KievBasePlugin} as a project extension named {@code kievRuntime}.
 *
 * <p>Example usage:
 *
 * <pre class='autoTested'>
 *     plugins {
 *         id 'kiev'
 *     }
 *
 *     repositories {
 *         mavenCentral()
 *     }
 *
 *     dependencies {
 *         implementation "org.codehaus.groovy:groovy-all:2.1.2"
 *     }
 *
 *     def kievClasspath = groovyRuntime.inferKievClasspath(configurations.compileClasspath)
 *     // The returned class path can be used to configure the 'kievClasspath' property of tasks
 *     // such as 'KievCompile' or 'Kievdoc', or to execute these and other Kiev tools directly.
 * </pre>
 */
public abstract class KievRuntime {
    private static final Logger LOGGER = Logging.getLogger(KievPlugin.class);
    private static final List<String> KIEV_LIBS = Arrays.asList("symade");

    private final ProjectInternal project;

    public KievRuntime(Project project) {
        this.project = (ProjectInternal)project;
    }

    /**
     * Searches the specified class path for Kiev Jars ({@code kiev(-indy)}, {@code kiev-all(-indy)}) and returns a corresponding class path for executing Kiev tools such as the Kiev
     * compiler and Kievdoc tool. The tool versions will match those of the Kiev Jars found. If no Kiev Jars are found on the specified class path, a class path with the contents of the {@code
     * kiev} configuration will be returned.
     *
     * <p>The returned class path may be empty, or may fail to resolve when asked for its contents.
     *
     * @param classpath a class path containing Kiev Jars
     * @return a corresponding class path for executing Kiev tools such as the Kiev compiler and Kievdoc tool
     */
    public FileCollection inferKievClasspath(final Iterable<File> classpath) {
        // alternatively, we could return project.getLayout().files(Runnable)
        // would differ in at least the following ways: 1. live 2. no autowiring
        return new LazilyInitializedFileCollection(project.getTaskDependencyFactory()) {

            @Override
            public String getDisplayName() {
                return "Kiev runtime classpath";
            }

            @Override
            public FileCollection createDelegate() {
                try {
                    return inferKievClasspath();
                } catch (RuntimeException e) {
                    return new FailingFileCollection(getDisplayName(), e);
                }
            }

            private FileCollection inferKievClasspath() {
                KievJarFile kievJar = findKievJarFile(classpath);
                if (kievJar == null) {
                    throw new GradleException(
                        String.format(
                            "Cannot infer Kiev class path because no Kiev Jar was found on class path: %s",
                                classpath //Iterables.toString(classpath)
                        )
                    );
                }

                //if (kievJar.isKievAll()) {
                    return project.getLayout().files(kievJar.getFile());
                //}

                //VersionNumber kievVersion = kievJar.getVersion();

                //return inferKievClasspath(kievVersion);
            }

            private void addKievDependency(String kievDependencyNotion, List<Dependency> dependencies, String otherDependency) {
                String notation = kievDependencyNotion.replace(":kiev:", ":" + otherDependency + ":");
                addDependencyTo(dependencies, notation);
            }

            private void addDependencyTo(List<Dependency> dependencies, String notation) {
                // project.getDependencies().create(String) seems to be the only feasible way to create a Dependency with a classifier
                dependencies.add(project.getDependencies().create(notation));
            }

            private FileCollection inferKievAllClasspath(String notation, VersionNumber kievVersion) {
                List<Dependency> dependencies = new ArrayList<>();
                addDependencyTo(dependencies, notation);
                return detachedRuntimeClasspath(dependencies.toArray(new Dependency[0]));
            }

            private FileCollection inferKievClasspath(VersionNumber kievVersion) {
                // We may already have the required pieces on classpath via localKiev()
                Set<String> kievJarNames = kievJarNamesFor(kievVersion);
                List<File> kievClasspath = collectJarsFromClasspath(classpath, kievJarNames);
                return project.getLayout().files(kievClasspath);
            }

            private Configuration detachedRuntimeClasspath(Dependency... dependencies) {
                Configuration classpath = project.getConfigurations().detachedConfiguration(dependencies);
                getJvmPluginServices().configureAsRuntimeClasspath(classpath);
                return classpath;
            }

            // let's override this so that delegate isn't created at autowiring time (which would mean on every build)
            @Override
            public void visitDependencies(TaskDependencyResolveContext context) {
                if (classpath instanceof Buildable) {
                    context.add(classpath);
                }
            }
        };
    }

    private static List<File> collectJarsFromClasspath(Iterable<File> classpath, Set<String> jarNames) {
        return stream(classpath.spliterator(), false)
            .filter(file -> jarNames.contains(file.getName()))
            .collect(toList());
    }

    private static Set<String> kievJarNamesFor(VersionNumber kievVersion) {
        return KIEV_LIBS.stream()
            .map(libName -> libName + "-" + kievVersion + ".jar")
            .collect(toSet());
    }

    @Nullable
    static KievJarFile findKievJarFile(Iterable<File> classpath) {
//        StringBuilder sb = new StringBuilder();
//        for (File f : classpath) {
//            //LOGGER.quiet("findKievJarFile classpath entry: "+f);
//            sb.append(f.toString());
//            sb.append(", ");
//        }
//        LOGGER.quiet("findKievJarFile in "+sb);
        for (File file : classpath) {
            KievJarFile symadeJar = KievJarFile.parse(file);
            if (symadeJar != null) {
                LOGGER.quiet("findKievJarFile found: '"+symadeJar.getFile()+"' version: "+symadeJar.getVersion());
                return symadeJar;
            }
        }
        return null;
    }

    private JvmPluginServices getJvmPluginServices() {
        return project.getServices().get(JvmPluginServices.class);
    }
}
