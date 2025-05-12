plugins {
    kotlin("jvm") version "1.9.20"
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.hyperether"
version = "1.0.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

intellij {
    version.set("2023.2.5") // Updated to a more stable version
    type.set("IC") // IC = IntelliJ IDEA Community, AI = Android Studio for full Android support
    plugins.set(listOf("Kotlin", "org.jetbrains.android"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("232")
        // todo remove untilBuild so any new version can install
        untilBuild.set("243.*")
    }

    publishPlugin {
        // Set your plugin publishing token
        // token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

kotlin {
    jvmToolchain(17)
}

// Ensure Kotlin compatibility
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        languageVersion = "1.9"
        apiVersion = "1.9"
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}