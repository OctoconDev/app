@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  id("com.android.kotlin.multiplatform.library")
}

kotlin {
  jvm("desktop")

  targets.named<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget>("android") {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    namespace = "app.octocon.kotlix"
  }

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
    val ktorVersion = "3.4.0"

    val commonMain by getting {
      dependencies {
        compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

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

    val mobile by creating {
      dependsOn(commonMain)
    }

    val wasmJsMain by getting {
      dependencies {
        compileOnly("io.ktor:ktor-client-js-wasm-js:$ktorVersion")
      }
    }

    val desktopMain by getting {
      dependencies {
        implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
      }
    }

    androidMain.dependsOn(mobile)
    iosMain.dependsOn(mobile)
  }
}
