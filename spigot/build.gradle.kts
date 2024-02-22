group = "org.reprogle"
version = "3.2.1"

extra["platform"] = "spigot"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

plugins {
    java
    `maven-publish`
    alias(libs.plugins.shadow)
    alias(libs.plugins.sonar)
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.spigot.api)
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
}

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