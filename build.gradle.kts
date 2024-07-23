// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.9.9")
    }
    repositories {
        google()
    }
}

plugins {
    id("com.android.application") version ("8.1.2") apply (false)
    id("com.android.library") version ("8.1.2") apply (false)
    id("org.jetbrains.kotlin.android") version ("2.0.0") apply (false)
    id("com.google.devtools.ksp") version "2.0.0-1.0.22" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id ("androidx.room") version "2.6.1" apply false
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}