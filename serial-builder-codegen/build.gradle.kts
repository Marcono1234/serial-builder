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
    jvmArgs = listOf("--add-opens", "jdk.compiler/com.sun.tools.javac.file=ALL-UNNAMED")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "marcono1234.serialization.serialbuilder.codegen.Main"
    }
}
