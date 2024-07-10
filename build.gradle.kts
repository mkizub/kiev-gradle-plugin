plugins {
    id("java")
    id("java-gradle-plugin")
    id("com.gradle.plugin-publish") version "1.2.1"
    id("maven-publish")
    id("groovy") // for tests
}

repositories {
    mavenCentral()
}

group = "org.symade.kiev"
version = "0.6.0-SNAPSHOT"

dependencies {
    testImplementation("junit:junit:4.13.1")
    testImplementation(platform("org.spockframework:spock-bom:2.3-groovy-3.0"))
    testImplementation("org.spockframework:spock-core")
    testImplementation("org.spockframework:spock-junit4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    website = "https://github.com/mkizub/symade"
    vcsUrl = "https://github.com/mkizub/symade.git"

    plugins {
        create("kievPlugin") {
            id = "kiev-gradle-plugin"
            displayName = "Kiev language gradle plugin"
            description = "SymADE is written in Kiev language (extension of Java), this plugin allows to use Kiev compiler in gradle build"
            tags = listOf("symade", "kiev")
            implementationClass = "org.symade.kiev.gradle.api.plugins.KievPlugin"
        }
    }
}

publishing {

    repositories {
        maven {
            name = "SymadeRepo"
            val releasesRepoUrl = layout.buildDirectory.dir("/SymadeRepo/releases")
            val snapshotsRepoUrl = layout.buildDirectory.dir("/SymadeRepo/snapshots")
            url = uri(if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl)
        }
    }
}