@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  wasmJs {
    moduleName = "octocon-app"
    browser {
      commonWebpackConfig {
        outputFileName = "octocon-app.js"
      }
    }
    useEsModules()
    binaries.executable()
  }

  sourceSets {
    val wasmJsMain by getting {
      dependencies {
        implementation(project(":shared"))

        implementation(compose.ui)
        implementation(compose.foundation)

        implementation("org.jetbrains.kotlinx:kotlinx-browser-wasm-js:0.3")
      }
    }
  }
}