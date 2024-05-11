group = "org.reprogle"
version = "3.3.1"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

plugins {
    java
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.sonar)
}

repositories {
    mavenCentral()
}

sonar {
    properties {
        property("sonar.projectKey", "honeypot")
        property("sonar.organization", "terrorbytetw")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.gradle.skipCompile", true)
    }
}

// This runs tasks for every subproject
subprojects {
    sonar {
        properties {
            property("sonar.projectKey", "honeypot")
            property("sonar.organization", "terrorbytetw")
            property("sonar.host.url", "https://sonarcloud.io")
            property("sonar.gradle.skipCompile", true)
        }
    }
}

tasks.register("thankYou") {
    doLast{
        println("\n\n\n\n\n\n\n\n\n\n\n\n====================================================================================================================================================")
        println("Thanks for downloading Honeypot! If you enjoy the project, consider rating it on one of the many sites I release on, or give it a star on GitHub.")
        println("Your compiled jar files are located in honeypot-{platform}/build/libs. Be sure to grab the jar file for the version you're actually planning to use!")
        println("====================================================================================================================================================")
    }
}

tasks.build {
    finalizedBy("thankYou")
}

defaultTasks("build")