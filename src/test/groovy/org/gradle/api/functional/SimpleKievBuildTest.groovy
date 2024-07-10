package org.gradle.api.functional

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Unroll
class SimpleKievBuildTest extends AbstractKievPluginSpecification {
    
    File srcMainKiev
    File simplePogo

    /**
     * super#setup is invoked automatically
     * @return
     */
    @Override
    def setup() {
        srcMainKiev = testProjectDir.newFolder('src', 'main', 'kiev')
    }

    def 'apply kiev plugin and compile [Gradle #gradleVersion]'() {
        given:
        buildScript << getBasicBuildScriptForTesting()

        simplePogo = new File(srcMainKiev, asPath('example', 'gradle', 'Simple.kj'))
        simplePogo.getParentFile().mkdirs()
        simplePogo << """
            package example.gradle
            
            class Simple {}"""

        when:
        GradleRunner runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('compileKiev', '-is')
                //.withGradleVersion(gradleVersion)

        BuildResult result = runner.build()
        
        then:
        //result.output.contains('Initializing gosuc compiler')
        result.task(":compileKiev").outcome == SUCCESS

        // Verify presence of JAVA_TOOL_OPTIONS sent to stderr does not fail task execution
        // JAVA_TOOL_OPTIONS is echoed to stderr... amazing.
        //result.output.contains('Picked up JAVA_TOOL_OPTIONS: -Duser.language=en')
        
        //did we actually compile anything?
        new File(testProjectDir.root, asPath(expectedOutputDir + ['main', 'example', 'gradle', 'Simple.class'])).exists()

        //where:
        //gradleVersion << gradleVersionsToTest
    }

}
