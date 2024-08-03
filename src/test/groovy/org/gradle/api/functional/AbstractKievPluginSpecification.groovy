package org.gradle.api.functional

import org.gradle.util.VersionNumber
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Shared
import spock.lang.Specification

abstract class AbstractKievPluginSpecification extends Specification {

    // These are the versions of gradle to iteratively test against
    // Locally, only test the latest.
    //@Shared
    //String[] gradleVersionsToTest = System.getenv().get('CI') != null ? getTestedVersions().plus(getGradleVersion()).sort() : [getGradleVersion()]

    //protected static final String LF = System.lineSeparator
    protected static final String FS = File.separator

    @Rule
    TemporaryFolder testProjectDir = new TemporaryFolder()

    //protected  URL kievVersionResource = this.class.classLoader.getResource("kievVersion.txt")

    String rootDir = System.getProperty("user.dir").replace('\\', '/')
    File buildScript
    List<String> expectedOutputKievDir = ['build', 'classes', 'kiev']
    List<String> expectedOutputJavaDir = ['build', 'classes', 'java']
    //Closure<List<String>> expectedOutputDir = { String gradleVersion ->
    //    List<String> retval = ['build', 'classes']
    //    if(VersionNumber.parse(gradleVersion) >= VersionNumber.parse('4.0')) {
    //        retval += 'kiev'
    //    }
    //    return retval
    //}
    
    protected String getBasicBuildScriptForTesting() {
        //String kievVersion = this.kievVersion
        String buildFileContent =
            """
            plugins {
                id 'kiev-gradle-plugin'
            }
            repositories {
                mavenLocal()
                mavenCentral()
            }
            dependencies {
                implementation files("${rootDir}/symade-06.jar")
                testImplementation group: 'junit', name: 'junit', version: '4.13'
            }
            //compileKiev.kievOptions.forkOptions.jvmArgs += ['-Xdebug', '-Xrunjdwp:transport=dt_socket,address=5005,server=y,suspend=y'] //debug on linux/OS X
            //kievdoc {
            //    kievDocOptions.verbose = true
            //}
            tasks.compileKiev {
                options.verbose = true
                options.debug = true
            }
            """
        return buildFileContent
    }

    def setup() {
        testProjectDir.create()
        buildScript = testProjectDir.newFile('build.gradle')
        print("build.gradle: "+buildScript+"\n")
    }

    //protected String getKievVersion() {
    //    return getKievVersion(kievVersionResource)
    //}

    //protected String getKievVersion(URL url) {
    //    return new BufferedReader(new FileReader(url.file)).lines().findFirst().get()
    //}
    
    /**
     * @param An array of files and directories
     * @return Delimited String of the values, joined as suitable for use in a classpath statement
     */
    protected String asPath(String... values) {
        return String.join(FS, values)
    }

    /**
     * @param An iterable of files and directories
     * @return Delimited String of the values, joined as suitable for use in a classpath statement
     */
    protected String asPath(List<String> values) {
        return asPath(values.toArray(new String[0]))
    }
    
    /**
     * 
     * @param An iterable of directories
     * @return Delimited String of the values, joined as suitable for use in a package statement
     */
    protected String asPackage(String... values) {
        return String.join(".", values)
    }
}
