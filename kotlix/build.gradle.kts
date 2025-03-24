@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("com.android.library")
}

kotlin {
  androidTarget()

  @OptIn(ExperimentalKotlinGradlePluginApi::class)
  compilerOptions {
    // https://kotlinlang.org/docs/multiplatform-expect-actual.html#expected-and-actual-classes
    freeCompilerArgs.add("-Xexpect-actual-classes")
  }

  /*listOf(
    iosArm64(),
    iosSimulatorArm64()
  ).forEach { iosTarget ->
    iosTarget.binaries.framework {
      baseName = "kotlix"
      isStatic = true
    }
  }*/

  iosArm64()
  iosSimulatorArm64()

  wasmJs { browser() }

  applyDefaultHierarchyTemplate()

  sourceSets {
    val ktorVersion = "3.0.3"

    val commonMain by getting {
      dependencies {
        compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

        // HTTP client
        compileOnly("io.ktor:ktor-client-core:$ktorVersion")
        compileOnly("io.ktor:ktor-client-content-negotiation:$ktorVersion")
        compileOnly("io.ktor:ktor-client-logging:$ktorVersion")
        compileOnly("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
      }
    }
    val androidMain by getting {
      dependencies {
        compileOnly("io.ktor:ktor-client-okhttp:$ktorVersion")
      }
    }
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    val iosMain by getting {
      dependencies {
        compileOnly("io.ktor:ktor-client-darwin:$ktorVersion")
      }
    }

    val wasmJsMain by getting {
      dependencies {
        compileOnly("io.ktor:ktor-client-js-wasm-js:$ktorVersion")
      }
    }

    val mobile by creating {
      dependsOn(commonMain)
    }
    androidMain.dependsOn(mobile)
    iosMain.dependsOn(mobile)
  }
}

android {
  compileSdk = (findProperty("android.compileSdk") as String).toInt()
  namespace = "app.octocon.common"

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
