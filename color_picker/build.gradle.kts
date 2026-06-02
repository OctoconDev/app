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

  // The androidTarget is already created by com.android.kotlin.multiplatform.library plugin
  targets.named<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget>("android") {
    namespace = "app.octocon.color_picker"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
  }

  iosArm64()
  iosSimulatorArm64()

  wasmJs { browser() }

  applyDefaultHierarchyTemplate()

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("com.github.ajalt.colormath:colormath:3.6.1")

        val composeVersion = findProperty("compose.version") as String
        val material3Version = findProperty("compose.material3.version") as String
        compileOnly("org.jetbrains.compose.runtime:runtime:$composeVersion")
        compileOnly("org.jetbrains.compose.foundation:foundation:$composeVersion")
        compileOnly("org.jetbrains.compose.material3:material3:$material3Version")
        compileOnly("org.jetbrains.compose.components:components-resources:$composeVersion")
      }
    }

    val iosMain by getting {
      dependencies {
        api("androidx.performance:performance-annotation:1.0.0-alpha01")
      }
    }
  }
}
