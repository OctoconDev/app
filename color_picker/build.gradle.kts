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
      baseName = "color_picker"
      isStatic = true
    }
  }*/

  iosArm64()
  iosSimulatorArm64()

  wasmJs { browser() }

  sourceSets {
    val commonMain by getting {
      dependencies {
        implementation("com.github.ajalt.colormath:colormath:3.5.0")

        compileOnly(compose.runtime)
        compileOnly(compose.foundation)
        // compileOnly(compose.material3)
        compileOnly("org.jetbrains.compose.material3:material3:1.8.0+dev2098")
        compileOnly(compose.components.resources)
      }
    }
  }
}

android {
  namespace = "app.octocon.color_picker"
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
