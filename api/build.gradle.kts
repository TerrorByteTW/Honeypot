group = "org.reprogle"
version = "3.2.0"

extra["platform"] = "api"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

plugins {
    java
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.spigot.api)
    compileOnly(libs.folia.api)
}