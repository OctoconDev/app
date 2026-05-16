@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  kotlin("multiplatform")
  id("com.android.application")
  // id("io.sentry.android.gradle").version("4.5.1")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.gms.google-services")
  id("androidx.baselineprofile")
  id("kotlin-parcelize")
}

kotlin {
  androidTarget()

  sourceSets {
    val androidMain by getting {
      dependencies {
        implementation(project(":shared"))
      }
    }
  }

  dependencies {
    val decomposeVersion = "3.4.0"
    implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("androidx.browser:browser:1.9.0")
    implementation("com.arkivanov.decompose:decompose:$decomposeVersion")

    implementation("com.nimbusds:nimbus-jose-jwt:10.8")

    // Widget
    implementation("androidx.work:work-runtime-ktx:2.11.1")
    implementation("io.coil-kt.coil3:coil:3.4.0")
    implementation("io.coil-kt.coil3:coil-network-ktor3:3.4.0")
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")
    compileOnly("com.materialkolor:material-kolor:2.1.1")
    compileOnly("org.jetbrains.compose.material3:material3:1.11.0-alpha03")
    compileOnly("org.jetbrains.compose.components:components-resources:1.11.0-alpha03")
    // compileOnly("org.jetbrains.compose.material3:material3:1.8.0+dev2098")

    val ktorVersion = "3.4.0"

    compileOnly("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

    // HTTP client
    compileOnly("io.ktor:ktor-client-core:$ktorVersion")
    compileOnly("io.ktor:ktor-client-content-negotiation:$ktorVersion")
    compileOnly("io.ktor:ktor-client-logging:$ktorVersion")
    compileOnly("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
  }
}

android {
  compileSdk = (findProperty("android.compileSdk") as String).toInt()
  namespace = "app.octocon"

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")

  defaultConfig {
    applicationId = "app.octocon.OctoconApp"
    minSdk = (findProperty("android.minSdk") as String).toInt()
    targetSdk = (findProperty("android.targetSdk") as String).toInt()
    versionCode = (findProperty("android.versionCode") as String).toInt()
    versionName = findProperty("android.versionName") as String
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  kotlin {
    jvmToolchain(17)
  }

  buildTypes {
    getByName("debug") {
      // applicationIdSuffix = ".debug"
    }
    getByName("release") {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(
        getDefaultProguardFile("proguard-android-optimize.txt"),
        "proguard-rules.pro"
      )
    }

    create("debugMinified") {
      initWith(getByName("release"))
      matchingFallbacks.add("release")

      signingConfig = signingConfigs.getByName("debug")
    }

    create("benchmark") {
      initWith(getByName("release"))
      matchingFallbacks.add("release")
      signingConfig = signingConfigs.getByName("debug")
      isShrinkResources = false
      isMinifyEnabled = false
    }
  }
  buildFeatures {
    viewBinding = true
  }
}

dependencies {
  implementation("androidx.profileinstaller:profileinstaller:1.3.1")
  "baselineProfile"(project(":baselineprofile"))
}