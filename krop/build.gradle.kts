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
    namespace = "app.octocon.krop"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
  }

  iosArm64()
  iosSimulatorArm64()

  wasmJs { browser() }

  applyDefaultHierarchyTemplate()

  sourceSets {
    val commonMain by getting {
      dependencies {
        val composeVersion = findProperty("compose.version") as String
        compileOnly("org.jetbrains.compose.runtime:runtime:$composeVersion")
        compileOnly("org.jetbrains.compose.foundation:foundation:$composeVersion")
        compileOnly("org.jetbrains.compose.material3:material3:$composeVersion")
        implementation("org.jetbrains.compose.material:material-icons-core:$composeVersion")
        implementation("org.jetbrains.compose.material:material-icons-extended:$composeVersion")
        // compileOnly("org.jetbrains.compose.material3:material3:1.8.0+dev2098")
        implementation("org.jetbrains.compose.components:components-resources:$composeVersion")
      }
    }
    val iosMain by getting {
      dependencies {
        api("androidx.performance:performance-annotation:1.0.0-alpha01")
      }
    }
  }
}

compose.resources {
  publicResClass = true
  packageOfResClass = "com.mr0xf00.easycrop.ui"
}