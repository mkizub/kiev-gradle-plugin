package org.gradle.api.internal.tasks

import org.symade.kiev.gradle.api.tasks.compile.KievCompile
import org.symade.kiev.gradle.api.plugins.KievBasePlugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static org.gradle.util.WrapUtil.toLinkedSet

class KievBasePluginTest extends Specification {

    File tmpRootDir
    private Project project

    private Project createRootProject(File rootDir) {
        return ProjectBuilder
                .builder()
                .withProjectDir(rootDir)
                .build()
    }

    void setup() {
        project = createRootProject()
        tmpRootDir = project.rootDir
        project.pluginManager.apply(KievBasePlugin)
    }

    void appliesTheJavaBasePluginToTheProject() {
        expect:
        project.getPlugins().hasPlugin(JavaBasePlugin)
    }

    void appliesMappingsToNewSourceSet() {
        def sourceSet = project.sourceSets.create('custom')
        expect:
        sourceSet.kiev.displayName == "custom Kiev source"
        sourceSet.kiev.srcDirs == toLinkedSet(project.file("src/custom/kiev"))
    }

    void addsCompileTaskToNewSourceSet() {
        project.sourceSets.create('custom')
        def task = project.tasks['compileCustomKiev']
        expect:
        task instanceof KievCompile
        task.description == 'Compiles the custom Kiev source.'
        //task.dependsOn.contains('compileCustomJava')
    }

}
