plugins {
    alias(libs.plugins.android.application) apply false
    // Not applied anywhere: declaring it pins the Kotlin Gradle plugin to libs.versions.kotlin.
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
}
