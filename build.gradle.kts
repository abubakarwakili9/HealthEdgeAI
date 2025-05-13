// Top-level build.gradle.kts
buildscript {
    // Explicitly define Kotlin version
    val kotlinVersion = "1.9.0" // Use the version that matches your Android Studio

    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    // Remove any KSP plugin declaration from here if present
}