@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvm("desktop")
    
    targets.named<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget>("android") {
        namespace = "com.mikepenz.markdown"
        compileSdk = (findProperty("android.compileSdk") as String).toInt()
    }

    iosArm64()
    iosSimulatorArm64()

    wasmJs { browser() }

    sourceSets {
        val commonMain by getting {
            dependencies {
                api("org.jetbrains:markdown:0.7.3")
                compileOnly(compose.runtime)
                compileOnly(compose.ui)
                compileOnly(compose.foundation)
            }
        }
    }
}
