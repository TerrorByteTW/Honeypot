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
version = "3.4.1"

extra["platform"] = "paper"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))

plugins {
    java
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.sonar)
    alias(libs.plugins.lombok)
}

repositories {
    mavenCentral()
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.folia.api)

    compileOnly(libs.vault)
    compileOnly(libs.placeholder.api)
    implementation(libs.boosted.yaml)
    implementation(libs.bstats)
    implementation(libs.spigui)
    implementation(project(":api"))

    compileOnly(libs.worldguard)
    compileOnly(libs.griefprevention)
    compileOnly(libs.lands)

    implementation(libs.guice)
    implementation(libs.okhttp)
}

// Configure the file output names of most files (Excluding shadow, which needs tweaking itself to avoid a "-all" being tagged onto it)
tasks.withType<Jar> {
    val platform: String by project.extra
    archiveBaseName.set("honeypot-${platform}")
}

// Build script for ensuring that everything is done correctly 
tasks.named("build") {
    dependsOn("processResources", "shadowJar", "javadoc")
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
    relocate("dev.dejvokep.boostedyaml", "org.reprogle.honeypot.common.libs.boostedyaml")
    relocate("org.bstats", "org.reprogle.honeypot.common.libs.bstats")
    relocate("com.samjakob", "org.reprogle.honeypot.common.libs.spigui")
}