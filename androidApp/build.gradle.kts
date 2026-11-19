import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.horsesgo"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.horsesgo"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        ndk.abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures.compose = true
}

kotlin.compilerOptions.jvmTarget.set(JvmTarget.JVM_17)

dependencies {
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
}
