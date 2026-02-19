import org.jetbrains.intellij.platform.gradle.TestFrameworkType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    kotlin("jvm") version "2.3.10"
    id("org.jetbrains.intellij.platform") version "2.11.0"
}

group = "com.hyperether"
version = "1.1.1"

repositories {
    mavenCentral()
    gradlePluginPortal()
    intellijPlatform {
        defaultRepositories()
    }
}

intellijPlatform {
    // Increase build timeout for newer IntelliJ versions
    buildSearchableOptions = false // Skip building searchable options to speed up development builds

    publishing {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "241"
        }

        changeNotes = """
            <ul>
                <li>Added support for drawables</li>
                <li>Added support for K2 Kotlin compiler</li>
                <li>Updated compatibility with IntelliJ 2025</li>
            </ul>
        """.trimIndent()
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    intellijPlatform {
        intellijIdea("2025.3.2")
        testFramework(TestFrameworkType.Platform)
        bundledPlugin("org.jetbrains.kotlin")
    }
}
