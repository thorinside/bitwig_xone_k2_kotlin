import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins {
    kotlin("jvm") version "2.0.21"
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "dev.nosuch"
version = "1.0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.bitwig.com")
    }
}

dependencies {
    implementation("com.bitwig:extension-api:19") // provided
    implementation("org.apache.commons:commons-lang3:3.17.0")

    implementation("org.slf4j:slf4j-api:2.0.16")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // development build
    if (project.hasProperty("dev")) {
        implementation("ch.qos.logback:logback-core:1.5.12")
        implementation("ch.qos.logback:logback-classic:1.5.12")
    }
}
defaultTasks("shadowJar")

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinJvmCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
    }
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveFileName.set("XoneK2.bwextension")
    dependencies {
        // exclude provided dependencies
        exclude(dependency("com.bitwig:extension-api:19"))
        exclude(dependency("org.apache.commons:commons-lang3:3.5"))
        if(!project.hasProperty("dev")) {
            exclude("logback.xml")
        }
    }
}

tasks.register<Copy>("installBwExtension") {
    dependsOn("clean", "shadowJar")
    from("build/libs") {
        include("*.bwextension")
    }
    // TODO platform specific
    into("${System.getProperty("user.home")}/Documents/Bitwig Studio/Extensions")
}

// for debugging on console:
//   gradle -Pdev start
tasks.register<Exec>("start") {
    dependsOn("installBwExtension")
    if (project.hasProperty("dev")) {
        environment("BITWIG_DEBUG_PORT", 8989)
    }
    // TODO platform specific
    commandLine("/Applications/Bitwig Studio.app/Contents/MacOS/BitwigStudio")
}

