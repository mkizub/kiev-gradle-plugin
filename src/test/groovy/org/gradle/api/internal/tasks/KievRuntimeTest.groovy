package org.gradle.api.internal.tasks

import org.symade.kiev.gradle.api.plugins.KievBasePlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class KievRuntimeTest extends Specification {

    def project = ProjectBuilder.builder().build()

    def setup() {
        project.pluginManager.apply(KievBasePlugin)
    }

    def 'inference fails if no repository declared'() {
        def kievClasspath = project.kievRuntime.inferKievClasspath([new File('other.jar'), new File('symade-06.jar')])
        print(kievClasspath.files.class)

        expect:
        kievClasspath.files.empty
    }

    def 'test to find Kiev Jars on class path'() {
        when:
        def file = project.kievRuntime.findKievJarFile([new File('other.jar'), new File('symade-06.jar'), new File('gosu-core-api-1.8.jar')])

        then:
        file.file.name == 'symade-06.jar'
    }

    def 'returns null if Kiev Jar not found'() {
        when:
        def file = project.kievRuntime.findKievJarFile([new File('other.jar'), new File('gosu-core-1.7.jar'), new File('gosu-core-api-1.8.jar')])

        then:
        file == null
    }
}
