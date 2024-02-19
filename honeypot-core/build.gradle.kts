group = "org.reprogle"
version = "3.1.1"

extra["platform"] = "spigot"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

plugins {
    java
    alias(libs.plugins.shadow)
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

    compileOnly(libs.worldguard)
    compileOnly(libs.griefprevention)
    compileOnly(libs.lands)
}