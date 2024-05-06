group = "org.reprogle"
version = "3.2.2"

extra["platform"] = "api"

java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))

plugins {
    java
    `maven-publish`
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

// Configure the file output names of most files (Excluding shadow, which needs tweaking itself to avoid a "-all" being tagged onto it)
tasks.withType<Jar> {
    val platform: String by project.extra
    archiveBaseName.set("honeypot-${platform}")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "${group}"
            artifactId = "honeypot-api"
            version = "${version}"

            from(components["java"])
        }
    }

    repositories {
        maven {
            name = "GithubPackages"
            url = uri("https://maven.pkg.github.com/TerrorByteTW/Honeypot")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}