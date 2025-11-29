// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    // MATCHING VERSION: We use 2.57.2 here, so App module must use 2.57.2
    id("com.google.dagger.hilt.android") version "2.57.2" apply false
    // KSP 2.0.21 is designed for Kotlin 2.0.x
    id("com.google.devtools.ksp") version "2.0.21-1.0.28" apply false
}