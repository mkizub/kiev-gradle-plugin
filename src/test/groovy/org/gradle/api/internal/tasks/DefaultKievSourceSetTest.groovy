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
package org.gradle.api.internal.tasks

import spock.lang.Specification

import org.gradle.api.Action
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.Project
import org.gradle.util.internal.CollectionUtils
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.internal.nativeintegration.services.NativeServices
import org.symade.kiev.gradle.internal.tasks.DefaultKievSourceSet
import org.symade.kiev.gradle.api.tasks.KievSourceSet
import org.symade.kiev.gradle.api.tasks.KievSourceDirectorySet;

import static org.gradle.api.reflect.TypeOf.typeOf

class DefaultKievSourceSetTest extends Specification {

    File tmpRootDir
    KievSourceSet sourceSet

    public Project createRootProject(File rootDir) {
        return ProjectBuilder
                .builder()
                .withProjectDir(rootDir)
                .build()
    }

    void setup() {
        Project project = createRootProject()
        tmpRootDir = project.rootDir
        NativeServices.initializeOnDaemon(tmpRootDir, NativeServices.NativeServicesMode.ENABLED)
        sourceSet = new DefaultKievSourceSet("<name>", "<display-name>", project.objects)
    }

    def defaultValues() {
        expect:
        sourceSet.kiev instanceof KievSourceDirectorySet
        sourceSet.kiev.isEmpty()
        sourceSet.kiev.name == '<name>'
        sourceSet.kiev.displayName == '<display-name> Kiev source'
        def includes = sourceSet.kiev.filter.includes
        includes.size() == 2 && includes.containsAll(['**/*.kj', '**/*.java'])
        sourceSet.kiev.filter.excludes.isEmpty()

        sourceSet.allKiev.isEmpty()
        sourceSet.allKiev.displayName =='<display-name> Kiev source'
        sourceSet.allKiev.srcDirs.containsAll(sourceSet.kiev.files)
        sourceSet.allKiev.filter.includes.containsAll(['**/*.kj'])
        sourceSet.allKiev.filter.excludes.isEmpty()
    }

    void canConfigureKievSource() {
        sourceSet.kiev {
            srcDir 'src/kiev'
        }
        expect:
        CollectionUtils.single(sourceSet.kiev.srcDirs) == new File(tmpRootDir, "src/kiev")
    }

    void canConfigureKievSourceUsingAnAction() {
        sourceSet.kiev({ set ->
            set.srcDir 'src/kiev'
        } as Action<SourceDirectorySet>)

        expect:
        CollectionUtils.single(sourceSet.kiev.srcDirs) == new File(tmpRootDir, "src/kiev")
    }

    void exposesConventionPublicType() {
        sourceSet.publicType == typeOf(KievSourceSet)
    }
}
