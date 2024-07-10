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
import org.gradle.api.file.SourceDirectorySet;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.internal.JavaPluginHelper;
import org.gradle.api.plugins.jvm.internal.JvmFeatureInternal;
import org.gradle.api.tasks.GroovySourceDirectorySet;
import org.gradle.api.tasks.javadoc.Groovydoc;
import org.symade.kiev.gradle.api.tasks.KievRuntime;

/**
 * <p>A {@link Plugin} which extends the {@link JavaPlugin} to provide support for compiling and documenting Groovy
 * source files.</p>
 *
 * @see <a href="https://docs.gradle.org/current/userguide/groovy_plugin.html">Groovy plugin reference</a>
 */
public abstract class KievPlugin implements Plugin<Project> {
    private static final Logger LOGGER = Logging.getLogger(KievPlugin.class);

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(KievBasePlugin.class);
        project.getPluginManager().apply(JavaPlugin.class);

//        configureGroovydoc(project);
    }

//    private void configureGroovydoc(final Project project) {
//        project.getTasks().register(GROOVYDOC_TASK_NAME, Groovydoc.class, groovyDoc -> {
//            groovyDoc.setDescription("Generates Groovydoc API documentation for the main source code.");
//            groovyDoc.setGroup(JavaBasePlugin.DOCUMENTATION_GROUP);
//
//            JvmFeatureInternal mainFeature = JavaPluginHelper.getJavaComponent(project).getMainFeature();
//            groovyDoc.setClasspath(mainFeature.getSourceSet().getOutput().plus(mainFeature.getSourceSet().getCompileClasspath()));
//
//            SourceDirectorySet groovySourceSet = mainFeature.getSourceSet().getExtensions().getByType(GroovySourceDirectorySet.class);
//            groovyDoc.setSource(groovySourceSet);
//        });
//    }
}
