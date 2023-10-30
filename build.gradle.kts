// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.2")
        classpath("com.google.gms:google-services:4.3.15")
    }
}
plugins {
    id("com.android.application") version ("8.1.2") apply (false)
    id("com.android.library") version ("8.1.2") apply (false)
    id("org.jetbrains.kotlin.android") version ("1.8.22") apply (false)
}

task<Delete>("clean") {
    delete(rootProject.buildDir)
}