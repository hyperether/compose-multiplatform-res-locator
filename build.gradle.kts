plugins {
    kotlin("jvm") version "2.0.0" // Latest Kotlin version with full K2 support
    id("org.jetbrains.intellij") version "1.17.1" // Latest intellij plugin version
}

group = "com.hyperether"
version = "1.0.3"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

intellij {
    version.set("2023.2.5")
    type.set("IC")
    plugins.set(listOf("Kotlin", "org.jetbrains.android"))
}

tasks {
    patchPluginXml {
        sinceBuild.set("241")
        untilBuild.set("251.*")
        changeNotes.set("""
            <ul>
                <li>Added support for K2 Kotlin compiler</li>
                <li>Updated compatibility with IntelliJ 2025</li>
            </ul>
        """.trimIndent())
    }

//    publishPlugin {
//        token.set(System.getenv("PUBLISH_TOKEN"))
//    }

    // Increase build timeout for newer IntelliJ versions
    buildSearchableOptions {
        enabled = false // Skip building searchable options to speed up development builds
    }
}

kotlin {
    jvmToolchain(17)
    // K2 is the default in Kotlin 2.0.0, no need for explicit settings
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        // No need to set languageVersion/apiVersion as K2 is now the default in 2.0.0
        // No need for -Xuse-k2 flag anymore
    }
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
//    implementation("org.jetbrains.kotlin:kotlin-analysis-api:2.0.0")
//    implementation("org.jetbrains.kotlin:kotlin-analysis-api-standalone:2.0.0")
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.2")

    // Add specific dependencies for 2025.1.1 compatibility
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:2.0.0")
}