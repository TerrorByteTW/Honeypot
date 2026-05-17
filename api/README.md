# Honeypot API

This is the API for Honeypot. Not only does it provide a surface for developers to integrate with Honeypot, it is also
used by Honeypot, itself, internally.

## Creating a Storage Provider?

It's recommended to use the Annotation Processor included with the Honeypot API to ensure your storage provider is
properly integrated and validated. The processor will generate the necessary code to register your storage provider with
Honeypot.

```kotlin
// build.gradle.kts if using Gradle
dependencies {
    // Other dependencies...
    annotationProcessor("org.reprogle.honeypot:api:[version]")
}
```

```xml
<!-- pom.xml if using Maven -->
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>[whatever the latest version is]</version>
            <configuration>
                <!-- Other Configuration... -->
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.reprogle.honeypot</groupId>
                        <artifactId>api</artifactId>
                        <version>[version]</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

This will validate your Storage Providers at build time to ensure they adhere to the Honeypot API contract and are ready
for use. Failing to do this may result in Honeypot refusing to register or use your Storage Provider at runtime.