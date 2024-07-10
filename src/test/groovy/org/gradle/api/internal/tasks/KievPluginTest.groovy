package org.gradle.api.internal.tasks

import org.gradle.api.plugins.internal.DefaultJavaPluginExtension
import org.symade.kiev.gradle.api.plugins.KievPlugin
import org.symade.kiev.gradle.api.tasks.KievSourceSet
import org.symade.kiev.gradle.api.tasks.compile.KievCompile
import org.gradle.api.Project
import org.gradle.api.internal.artifacts.configurations.Configurations
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.plugins.internal.DefaultJavaPluginConvention
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.reflect.Instantiator
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

import java.util.concurrent.Callable

import static org.gradle.util.WrapUtil.toLinkedSet
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.*

class KievPluginTest extends Specification {

    File tmpRootDir
    KievSourceSet sourceSet

    public Project createRootProject(File rootDir) {
        return ProjectBuilder
                .builder()
                .withProjectDir(rootDir)
                .build()
    }

    private Project project
    //private Instantiator instantiator
    //private DefaultJavaPluginExtension convention

    @Before
    public void applyPlugin() throws IOException {
        project = createRootProject()
        //instantiator = ((ProjectInternal) project).services.get(Instantiator)
        //convention = new DefaultJavaPluginConvention(((ProjectInternal) project), instantiator, null)
        project.pluginManager.apply(KievPlugin)
    }

    void appliesTheJavaPluginToTheProject() {
        expect:
        project.plugins.hasPlugin(JavaPlugin)
    }

    void addsKievConfigurationToTheProject() {
        def configuration = project.configurations.getByName(JavaPlugin.COMPILE_CLASSPATH_CONFIGURATION_NAME)
        expect:
        assertThat(Configurations.getNames(configuration.extendsFrom), Matchers.emptyIterable())
        assertFalse(configuration.visible)
        assertTrue(configuration.transitive)
    }

    void addsKievConventionToEachSourceSet() {
        def mainSourceSet = project.sourceSets.main
        def testSourceSet = project.sourceSets.test
        expect:
        assertThat(mainSourceSet.kiev.displayName, equalTo('main Kiev source'))
        assertThat(mainSourceSet.kiev.srcDirs, equalTo(toLinkedSet(project.file('src/main/kiev'))))

        assertThat(testSourceSet.kiev.displayName, equalTo('test Kiev source'))
        assertThat(testSourceSet.kiev.srcDirs, equalTo(toLinkedSet(project.file('src/test/kiev'))))
    }

    void addsCompileTaskForEachSourceSet() {
        def compileTask = project.tasks['compileKiev']
        def compileTestTask = project.tasks['compileTestKiev']
        expect:
        compileTask instanceof KievPlugin
        assertThat(compileTask.description, equalTo('Compiles the main Kiev source.'))
        //assertTrue(compileTask.dependsOn.contains(JavaPlugin.COMPILE_JAVA_TASK_NAME))

        assertThat(compileTestTask, instanceOf(KievPlugin))
        assertThat(compileTestTask.description, equalTo('Compiles the test Kiev source.'))
        //assertTrue(compileTestTask.dependsOn.contains(JavaPlugin.COMPILE_TEST_JAVA_TASK_NAME))
        //assertTrue(task.dependsOn.contains(JavaPlugin.CLASSES_TASK_NAME)) //TODO failing; do we care?
    }


//    void canConfigureSourceSets() {
//        File dir = new File('classes-dir')
//        convention.sourceSets {
//            main {
//                output.addClassesDir ( new Callable() {
//                    public Object call() {
//                        return dir;
//                    } })
//            }
//        }
//        expect:
//        convention.sourceSets.main.output.classesDirs.containsAll(project.file(dir))
//    }

    void canConfigureMainKievClosure() {
        File dir = new File('path/to/POGOs')
        project.sourceSets {
            main {
                kiev.srcDirs = [ dir ] // kiev typeis org.gradle.api.internal.file.DefaultSourceDirectorySet
            }
        }
        expect:
        project.sourceSets.main.kiev.srcDirs == toLinkedSet(project.file(dir))
    }

    void canConfigureMainKievClosureSrcDirsSingleArg() {
        String dirAsString = 'path/to/POGOs'
        project.sourceSets {
            main {
                kiev {
                    srcDirs dirAsString //kiev typeis org.gradle.api.internal.file.DefaultSourceDirectorySet
                }
            }
        }
        expect:
        project.sourceSets.main.kiev.srcDirs == toLinkedSet(project.file('src/main/kiev'), project.file(dirAsString))
    }

    void canConfigureMainKievClosureSrcDirsMultipleArg() {
        String dirAsString = 'path/to/POGOs'
        String anotherSource = 'some/more/kiev/files'
        project.sourceSets {
            main {
                kiev {
                    srcDirs dirAsString, anotherSource
                }
            }
        }
        expect:
        project.sourceSets.main.kiev.srcDirs ==toLinkedSet(project.file('src/main/kiev'), project.file(dirAsString), project.file(anotherSource))
    }

    void canConfigureMainKievClosureSrcDirSingular() {
        String dirAsString = 'path/to/POGOs'
        project.sourceSets {
            main {
                kiev {
                    srcDir dirAsString //kiev typeis org.gradle.api.internal.file.DefaultSourceDirectorySet
                }
            }
        }
        expect:
        project.sourceSets.main.kiev.srcDirs == toLinkedSet(project.file('src/main/kiev'), project.file(dirAsString))
    }

    void canConfigureMainKievClosureSrcDirMultiple() {
        String dirAsString = 'path/to/POGOs'
        String anotherSource = 'some/more/kiev/filezz'
        project.sourceSets {
            main {
                kiev {
                    srcDir dirAsString
                    srcDir anotherSource
                }
            }
        }
        expect:
        project.sourceSets.main.kiev.srcDirs == toLinkedSet(project.file('src/main/kiev'), project.file(dirAsString), project.file(anotherSource))
    }

    void canConfigureMainKievClosureSrcDirAndSrcDirs() {
        String dirAsString = 'path/to/POGOs'
        String anotherSource = 'some/more/kiev/filezz'
        String aThirdSource = 'formerly/gscript'
        project.sourceSets {
            main {
                kiev {
                    srcDir dirAsString
                    srcDirs anotherSource, aThirdSource
                }
            }
        }
        expect:
        project.sourceSets.main.kiev.srcDirs == toLinkedSet(project.file('src/main/kiev'), project.file(dirAsString), project.file(anotherSource), project.file(aThirdSource))
    }

//    /**
//     * Get the default fork setting, then reverse it
//     * Verify failOnError defaults to true, then reverse it
//     */
//    void canConfigureCompileOptionsForJava() {
//        def isFork = project.tasks.compileJava.options.fork
//        assertThat(project.tasks.compileJava.options.failOnError, equalTo(true))
//        project.tasks.withType(JavaCompile.class) {
//            options.fork = !isFork
//            options.failOnError = false
//        }
//        assertThat(project.tasks.compileJava.options.fork, equalTo(!isFork))
//        assertThat(project.tasks.compileJava.options.failOnError, equalTo(false))
//    }

//    /**
//     * Get the default fork setting, then reverse it
//     * Verify failOnError defaults to true, then reverse it
//     */
//    @Test
//    void canConfigureCompileOptionsForKiev() {
//        def isFork = project.tasks.compileKiev.options.fork
//        assertThat(project.tasks.compileKiev.options.failOnError, equalTo(true))
//        project.tasks.withType(KievPlugin.class) {
//            options.fork = !isFork
//            options.failOnError = false
//        }
//        assertThat(project.tasks.compileKiev.options.fork, equalTo(!isFork))
//        assertThat(project.tasks.compileKiev.options.failOnError, equalTo(false))
//    }

}
