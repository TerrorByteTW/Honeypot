group = "org.reprogle"
version = "3.1.0"

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
        property("sonar.organization", "terrorbytetw")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.gradle.skipCompile", true)
    }
}

// This runs tasks for every subproject
subprojects {

    plugins.apply("java")
    plugins.apply("maven-publish")
    plugins.apply("com.github.johnrengelman.shadow")

    afterEvaluate {

        // Configure the Java plugin to output a javadoc jar and a sources jar
        java {
            withSourcesJar()
            withJavadocJar()
        }

        // This outputs the javadoc in an HTML format
        tasks.javadoc {
            destinationDir = file("${buildDir}/docs/javadoc")
        }

        // Configure the file output names of most files (Excluding shadow, which needs tweaking itself to avoid a "-all" being tagged onto it)
        tasks.withType<Jar> {
            val platform: String by project.extra
            archiveBaseName.set("honeypot-${platform}")
        }

        // Delete the file built with the normal build tasks, generated by Java.
        tasks.register("deleteFileFromBuildLibs") {
            doLast {
                val fileToDelete = file("$buildDir/libs/honeypot-core-3.1.0.jar")
                if(fileToDelete.exists()) {
                    fileToDelete.delete()
                    println("File deleted: ${fileToDelete.absolutePath}")
                } else {
                    println("File not found: ${fileToDelete.absolutePath}")
                }
            }
        }
        
        // Build script for ensuring that everything is done correctly 
        tasks.named("build") {
            dependsOn("processResources", "shadowJar", "deleteFileFromBuildLibs", "javadoc")
        }

        // Replaces the version number in the plugin.yml by expanding all variables to project properties
        tasks.processResources {
            filesMatching("plugin.yml") {
                expand(project.properties)
            }
        }

        // Equivalent of maven shade
        tasks.shadowJar {
            val platform: String by project.extra
            archiveBaseName.set("honeypot-${platform}")
            archiveClassifier.set("")
            relocate("dev.dejvokep.boostedyaml", "org.reprogle.honeypot.common.libs")
            relocate("org.bstats", "org.reprogle.honeypot.common.libs")
        }

        // We don't want shadowJar to be ran independently. If it is, warn the user
        gradle.taskGraph.whenReady {
            tasks.named("shadowJar") {
                doFirst {
                    if (!gradle.taskGraph.hasTask(":build") && !gradle.taskGraph.hasTask(":publish")) throw GradleException("If you are running the shadowJar task directly, don't! You'll have problems. Please run ./gradlew to actually build the project properly.")
                }
            }
        }

    }

    publishing {
        repositories {
            maven {
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GITHUB_TOKEN")
                }
                url = uri("https://maven.pkg.github.com/TerrorByteTW/Honeypot")
            }
        }
        publications {
            create<MavenPublication>("maven") {
                groupId = "${project.group}"
                artifactId = project.name
                version = "${project.version}"
                from(components["java"])
            }
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