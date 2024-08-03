package org.gradle.api.functional

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import spock.lang.Unroll

import static org.gradle.testkit.runner.TaskOutcome.SUCCESS

@Unroll
class MixedKievBuildTest extends AbstractKievPluginSpecification {
    
    File srcMainKiev
    File srcMainJava

    /**
     * super#setup is invoked automatically
     * @return
     */
    @Override
    def setup() {
        srcMainKiev = testProjectDir.newFolder('src', 'main', 'kiev')
        srcMainJava = testProjectDir.newFolder('src', 'main', 'java')
    }

    def 'compile java and kiev independant'() {
        given:
        buildScript << getBasicBuildScriptForTesting()

        File simpleKievFile = new File(srcMainKiev, asPath('example', 'gradle', 'Kiev.kj'))
        simpleKievFile.getParentFile().mkdirs()
        simpleKievFile << """
            package example.gradle;
            class Kiev {}"""

        File simpleJavaFile = new File(srcMainJava, asPath('example', 'gradle', 'Java.java'))
        simpleJavaFile.getParentFile().mkdirs()
        simpleJavaFile << """
            package example.gradle;
            class Java {}"""

        when:
        GradleRunner runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('compileKiev', 'compileJava', '-is')
                //.withGradleVersion(gradleVersion)

        BuildResult result = runner.build()
        
        then:
        //result.output.contains('Initializing gosuc compiler')
        result.task(":compileKiev").outcome == SUCCESS
        result.task(":compileJava").outcome == SUCCESS

        // Verify presence of JAVA_TOOL_OPTIONS sent to stderr does not fail task execution
        // JAVA_TOOL_OPTIONS is echoed to stderr... amazing.
        //result.output.contains('Picked up JAVA_TOOL_OPTIONS: -Duser.language=en')
        
        //did we actually compile anything?
        new File(testProjectDir.root, asPath(expectedOutputKievDir + ['main', 'example', 'gradle', 'Kiev.class'])).exists()
        new File(testProjectDir.root, asPath(expectedOutputJavaDir + ['main', 'example', 'gradle', 'Java.class'])).exists()

        //where:
        //gradleVersion << gradleVersionsToTest
    }

    def 'compile java dependant on kiev'() {
        given:
        String script = getBasicBuildScriptForTesting()
        script += """
            tasks.compileJava {
                classpath += files(sourceSets.main.kiev.classesDirectory)
            }
        """
        buildScript << script

        File simpleKievFile = new File(srcMainKiev, asPath('example', 'gradle', 'Kiev.kj'))
        simpleKievFile.getParentFile().mkdirs()
        simpleKievFile << """
            package example.gradle;
            class Kiev {}
        """

        File simpleJavaFile = new File(srcMainJava, asPath('example', 'gradle', 'Java.java'))
        simpleJavaFile.getParentFile().mkdirs()
        simpleJavaFile << """
            package example.gradle;
            class Java extends Kiev {}
        """

        when:
        GradleRunner runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('compileJava', 'compileKiev', '-is')

        BuildResult result = runner.build()

        then:
        result.task(":compileKiev").outcome == SUCCESS
        result.task(":compileJava").outcome == SUCCESS

        //did we actually compile anything?
        new File(testProjectDir.root, asPath(expectedOutputKievDir + ['main', 'example', 'gradle', 'Kiev.class'])).exists()
        new File(testProjectDir.root, asPath(expectedOutputJavaDir + ['main', 'example', 'gradle', 'Java.class'])).exists()
    }

    def 'compile kiev dependant on java'() {
        given:
        String script = getBasicBuildScriptForTesting()
        script += """
            tasks.compileKiev {
                classpath += files(sourceSets.main.java.classesDirectory)
            }
        """
        buildScript << script

        File simpleKievFile = new File(srcMainKiev, asPath('example', 'gradle', 'Kiev.kj'))
        simpleKievFile.getParentFile().mkdirs()
        simpleKievFile << """
            package example.gradle;
            class Kiev extends Java {}"""

        File simpleJavaFile = new File(srcMainJava, asPath('example', 'gradle', 'Java.java'))
        simpleJavaFile.getParentFile().mkdirs()
        simpleJavaFile << """
            package example.gradle;
            class Java {}
        """

        when:
        GradleRunner runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('compileKiev', 'compileJava', '-is')

        BuildResult result = runner.build()

        then:
        result.task(":compileKiev").outcome == SUCCESS
        result.task(":compileJava").outcome == SUCCESS

        //did we actually compile anything?
        new File(testProjectDir.root, asPath(expectedOutputKievDir + ['main', 'example', 'gradle', 'Kiev.class'])).exists()
        new File(testProjectDir.root, asPath(expectedOutputJavaDir + ['main', 'example', 'gradle', 'Java.class'])).exists()
    }

    def 'compile java depends on kiev with java files'() {
        given:
        String script = getBasicBuildScriptForTesting()
        script += """
            tasks.compileJava {
                classpath += files(sourceSets.main.kiev.classesDirectory)
            }
        """
        buildScript << script

        File simpleKievFile = new File(srcMainKiev, asPath('example', 'gradle', 'Kiev.kj'))
        simpleKievFile.getParentFile().mkdirs()
        simpleKievFile << """
            package example.gradle;
            class Kiev {}
        """

        File simpleJavaInKievFile = new File(srcMainKiev, asPath('example', 'gradle', 'JavaInKiev.java'))
        simpleJavaInKievFile.getParentFile().mkdirs()
        simpleJavaInKievFile << """
            package example.gradle;
            class JavaInKiev extends Kiev {}
        """

        File simpleJavaInJavaFile = new File(srcMainJava, asPath('example', 'gradle', 'Java.java'))
        simpleJavaInJavaFile.getParentFile().mkdirs()
        simpleJavaInJavaFile << """
            package example.gradle;
            class Java extends JavaInKiev {}
        """

        when:
        GradleRunner runner = GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withPluginClasspath()
                .withArguments('compileJava', 'compileKiev', '-is')

        BuildResult result = runner.build()

        then:
        result.task(":compileKiev").outcome == SUCCESS
        result.task(":compileJava").outcome == SUCCESS

        //did we actually compile anything?
        new File(testProjectDir.root, asPath(expectedOutputKievDir + ['main', 'example', 'gradle', 'Kiev.class'])).exists()
        new File(testProjectDir.root, asPath(expectedOutputKievDir + ['main', 'example', 'gradle', 'JavaInKiev.class'])).exists()
        new File(testProjectDir.root, asPath(expectedOutputJavaDir + ['main', 'example', 'gradle', 'Java.class'])).exists()
    }

}
