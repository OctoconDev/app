@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  kotlin("multiplatform")
  id("com.android.library")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  androidTarget()

  /*listOf(
    iosArm64(),
    iosSimulatorArm64()
  ).forEach {
    it.binaries.framework {
      baseName = "markdown_m3"
      isStatic = true
    }
  }*/

  iosArm64()
  iosSimulatorArm64()

  wasmJs { browser() }

  sourceSets {
    val commonMain by getting {
      dependencies {
        api(project(":multiplatform-markdown-renderer"))
        api("org.jetbrains:markdown:0.7.3")
        compileOnly(compose.runtime)
        // compileOnly(compose.material3)
        compileOnly("org.jetbrains.compose.material3:material3:1.8.0+dev2098")
      }
    }
  }
}

android {
  namespace = "com.mikepenz.markdown.m3"
  compileSdk = (findProperty("android.compileSdk") as String).toInt()
  defaultConfig {
    minSdk = (findProperty("android.minSdk") as String).toInt()
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  kotlin {
    jvmToolchain(17)
  }
}
