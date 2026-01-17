/*
 * Honeypot is a plugin written for Paper which assists with griefing auto-moderation
 *
 * Copyright (c) TerrorByte and Honeypot Contributors 2022 - 2025.
 *
 * This program is free software: You can redistribute it and/or modify it under
 *  the terms of the Mozilla Public License 2.0 as published by the Mozilla under the Mozilla Foundation.
 *
 * This program is distributed in the hope that it will be useful, but provided on an "as is" basis,
 * without warranty of any kind, either expressed, implied, or statutory, including,
 * without limitation, warranties that the Covered Software is free of defects, merchantable,
 * fit for a particular purpose or non-infringing. See the MPL 2.0 license for more details.
 *
 * For a full copy of the license in its entirety, please visit <https://www.mozilla.org/en-US/MPL/2.0/>
 */

group = "org.reprogle"
version = "3.5.1"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

plugins {
    java
    `maven-publish`
    alias(libs.plugins.shadow)
    idea
}

repositories {
    mavenCentral()
}

idea {
    module {
        inheritOutputDirs = false
        outputDir = sourceSets.main.get().output.classesDirs.first()
        testOutputDir = sourceSets.test.get().output.classesDirs.first()
    }
}

tasks.register("thankYou") {
    doLast {
        println("\n\n\n\n\n\n\n\n\n\n\n\n====================================================================================================================================================")
        println("Thanks for downloading Honeypot! If you enjoy the project, consider rating it on one of the many sites I release on, or give it a star on GitHub.")
        println("Your compiled jar files are located in honeypot-{platform}/build/libs. Be sure to grab the jar file for the version you're actually planning to use!")
        println("====================================================================================================================================================")
    }
}

tasks.build {
    finalizedBy("thankYou")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()
            from(components["java"])
        }
    }
}

defaultTasks("build")