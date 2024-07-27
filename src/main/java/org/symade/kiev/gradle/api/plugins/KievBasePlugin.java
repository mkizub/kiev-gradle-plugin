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

package org.symade.kiev.gradle.api.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.plugins.DslObject;
import org.gradle.api.internal.tasks.DefaultSourceSet;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.internal.JvmPluginsHelper;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.internal.deprecation.DeprecationLogger;
import org.gradle.jvm.toolchain.JavaLauncher;
import org.gradle.jvm.toolchain.JavaToolchainService;
import org.symade.kiev.gradle.internal.tasks.DefaultKievSourceSet;
import org.symade.kiev.gradle.api.tasks.KievRuntime;
import org.symade.kiev.gradle.api.tasks.KievSourceDirectorySet;
import org.symade.kiev.gradle.api.tasks.compile.KievCompile;

import javax.inject.Inject;

import java.io.File;
import java.util.Set;

import static org.gradle.api.internal.lambdas.SerializableLambdas.spec;

public abstract class KievBasePlugin implements Plugin<Project> {
    public static final String KIEV_RUNTIME_EXTENSION_NAME = "kievRuntime";

    private final ObjectFactory objectFactory;

    @Inject
    public KievBasePlugin(ObjectFactory objectFactory) {
        this.objectFactory = objectFactory;
    }

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(JavaBasePlugin.class);

        KievRuntime kievRuntime = project.getExtensions().create(KIEV_RUNTIME_EXTENSION_NAME, KievRuntime.class, project);

        configureCompileDefaults(project, kievRuntime);
        configureSourceSetDefaults(project);
    }

    private void configureCompileDefaults(Project project, KievRuntime kievRuntime) {
        project.getTasks().withType(KievCompile.class).configureEach(compile -> {
            compile.getConventionMapping().map(
                "kievClasspath",
                () -> kievRuntime.inferKievClasspath(compile.getClasspath())
            );

//            DefaultJavaPluginExtension javaExtension = (DefaultJavaPluginExtension) project.getExtensions().getByType(JavaPluginExtension.class);
//            JvmPluginsHelper.configureCompileDefaults(compile, javaExtension, (@Nullable JavaVersion rawConvention, Supplier<JavaVersion> javaVersionSupplier) -> {
//                if (rawConvention != null) {
//                    return rawConvention;
//                }
//                return JavaVersion.toVersion(compile.getJavaLauncher().get().getMetadata().getLanguageVersion().toString());
//            });
        });
    }

    private void configureSourceSetDefaults(Project project) {
        javaPluginExtension(project).getSourceSets().all(sourceSet -> {

            KievSourceDirectorySet kievSource = getKievSourceDirectorySet(sourceSet);
            sourceSet.getExtensions().add(KievSourceDirectorySet.class, "kiev", kievSource);
            kievSource.srcDir("src/" + sourceSet.getName() + "/kiev");

            // Explicitly capture only a FileCollection in the lambda below for compatibility with configuration-cache.
            final FileCollection kievSourceFiles = kievSource;
            sourceSet.getResources().getFilter().exclude(
                spec(element -> kievSourceFiles.contains(element.getFile()))
            );
            //sourceSet.getAllJava().source(kievSource);
            sourceSet.getAllSource().source(kievSource);

            TaskProvider<KievCompile> compileTask = createKievCompileTask(project, sourceSet, kievSource);

            ConfigurationContainer configurations = project.getConfigurations();
            configureLibraryElements(sourceSet, configurations, project.getObjects());
            configureTargetPlatform(compileTask, sourceSet, configurations);
        });
    }

    /**
     * In 9.0, once {@link org.gradle.api.internal.tasks.DefaultGroovySourceSet} is removed, we can update this to only construct the source directory
     * set instead of the entire source set.
     */
    @SuppressWarnings("deprecation")
    private KievSourceDirectorySet getKievSourceDirectorySet(SourceSet sourceSet) {
        final DefaultKievSourceSet kievSourceSet = objectFactory.newInstance(DefaultKievSourceSet.class, "kiev", ((DefaultSourceSet) sourceSet).getDisplayName(), objectFactory);
        DeprecationLogger.whileDisabled(() ->
            new DslObject(sourceSet).getConvention().getPlugins().put("kiev", kievSourceSet)
        );
        return kievSourceSet.getKiev();
    }

    private static void configureLibraryElements(SourceSet sourceSet, ConfigurationContainer configurations, ObjectFactory objectFactory) {
        // Explain that Groovy, for compile, also needs the resources (#9872)
        configurations.getByName(sourceSet.getCompileClasspathConfigurationName()).attributes(attrs ->
            attrs.attribute(
                LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE,
                objectFactory.named(LibraryElements.class, LibraryElements.CLASSES_AND_RESOURCES)
            )
        );
    }

    private void configureTargetPlatform(TaskProvider<KievCompile> compileTask, SourceSet sourceSet, ConfigurationContainer configurations) {
//        jvmLanguageUtils.useDefaultTargetPlatformInference(configurations.getByName(sourceSet.getCompileClasspathConfigurationName()), compileTask);
//        jvmLanguageUtils.useDefaultTargetPlatformInference(configurations.getByName(sourceSet.getRuntimeClasspathConfigurationName()), compileTask);
    }

    private TaskProvider<KievCompile> createKievCompileTask(Project project, SourceSet sourceSet, KievSourceDirectorySet kievSource) {
        final TaskProvider<KievCompile> compileTask = project.getTasks().register(sourceSet.getCompileTaskName("kiev"), KievCompile.class, kievCompile -> {
            //JvmPluginsHelper.compileAgainstJavaOutputs(kievCompile, sourceSet, objectFactory);
            //JvmPluginsHelper.configureAnnotationProcessorPath(sourceSet, kievSource, kievCompile.getOptions(), project);
            kievCompile.setDescription("Compiles the " + kievSource + ".");
            kievCompile.setSource(kievSource);
            kievCompile.getJavaLauncher().convention(getJavaLauncher(project));

            //kievCompile.getKievOptions().getDisabledGlobalASTTransformations().convention(Sets.newHashSet("kiev.grape.GrabAnnotationTransformation"));
        });
        JvmPluginsHelper.configureOutputDirectoryForSourceSet(sourceSet, kievSource, project, compileTask, compileTask.map(KievCompile::getOptions));

        // TODO: `classes` should be a little more tied to the classesDirs for a SourceSet so every plugin
        // doesn't need to do this.
        project.getTasks().named(sourceSet.getClassesTaskName(), task -> task.dependsOn(compileTask));

        return compileTask;
    }

    private static Provider<JavaLauncher> getJavaLauncher(Project project) {
        final JavaPluginExtension extension = javaPluginExtension(project);
        final JavaToolchainService service = extensionOf(project, JavaToolchainService.class);
        return service.launcherFor(extension.getToolchain());
    }

    private static JavaPluginExtension javaPluginExtension(Project project) {
        return extensionOf(project, JavaPluginExtension.class);
    }

    private static <T> T extensionOf(ExtensionAware extensionAware, Class<T> type) {
        return extensionAware.getExtensions().getByType(type);
    }
}
