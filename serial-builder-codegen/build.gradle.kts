plugins {
    `java-library`
    `maven-publish`
}

repositories {
    mavenCentral()
}

group = "marcono1234.serialization"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
    // Publish sources and javadoc
    withSourcesJar()
    withJavadocJar()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    // Used to compile generated code
    testImplementation("net.openhft:compiler:2.21ea81")
    testImplementation("org.slf4j:slf4j-simple:1.7.33")
    testImplementation(project(":serial-builder"))
}

tasks.test {
    useJUnitPlatform()

    // Temporary workaround for https://github.com/OpenHFT/Java-Runtime-Compiler/issues/91
    jvmArgs = listOf(
        "--add-opens", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED",
        "--add-opens", "java.base/java.lang=ALL-UNNAMED",
    )
}

tasks.javadoc {
    options {
        // Cast to standard doclet options, see https://github.com/gradle/gradle/issues/7038#issuecomment-448294937
        this as StandardJavadocDocletOptions

        encoding = "UTF-8"
        // Enable doclint, but ignore warnings for missing tags, see
        // https://docs.oracle.com/en/java/javase/17/docs/specs/man/javadoc.html#additional-options-provided-by-the-standard-doclet
        // The Gradle option methods are rather misleading, but a boolean `true` value just makes sure the flag
        // is passed to javadoc, see https://github.com/gradle/gradle/issues/2354
        addBooleanOption("Xdoclint:all,-missing", true)
    }

    shouldRunAfter(tasks.test)
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "marcono1234.serialization.serialbuilder.codegen.Main"
    }
}

// Make build reproducible
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}
