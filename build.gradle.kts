group = "org.reprogle"
version = "3.1.0"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

plugins {
    java
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

subprojects {
    afterEvaluate {
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
        
        tasks.named("build") {
            dependsOn("processResources", "shadowJar", "deleteFileFromBuildLibs")
        }

        tasks.processResources {
            filesMatching("plugin.yml") {
                expand(project.properties)
            }
        }

        tasks.shadowJar {
            val platform: String by project.extra
            archiveBaseName.set("honeypot-${platform}-${version}")
            archiveClassifier.set("")
            archiveVersion.set("")
            relocate("dev.dejvokep.boostedyaml", "org.reprogle.honeypot.common.libs")
            relocate("org.bstats", "org.reprogle.honeypot.common.libs")
        }

        // We don't want shadowJar to be ran independently. If it is, warn the user
        gradle.taskGraph.whenReady {
            tasks.named("shadowJar") {
                doFirst {
                    if (gradle.taskGraph.hasTask(":build")) {
                        println("shadowJar was run as a dependency of build")
                    } else {
                        throw new GradleException("If you are running the shadowJar task directly, don't! You'll have problems. Please run ./gradlew to actually build the project properly.")
                    }
                }
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