@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
  id("com.android.application")
  // id("io.sentry.android.gradle").version("4.5.1")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.google.gms.google-services")
  id("kotlin-parcelize")
}

dependencies {
  implementation(project(":shared"))
  val decomposeVersion = "3.4.0"
  implementation(platform("com.google.firebase:firebase-bom:34.10.0"))
  implementation("com.google.firebase:firebase-messaging")
  implementation("androidx.browser:browser:1.9.0")
  implementation("com.arkivanov.decompose:decompose:$decomposeVersion")

  implementation("com.nimbusds:nimbus-jose-jwt:10.8")

  val composeVersion = findProperty("compose.version") as String

  // Widget
  implementation("androidx.work:work-runtime-ktx:2.11.1")
  implementation("io.coil-kt.coil3:coil:3.4.0")
  implementation("io.coil-kt.coil3:coil-network-ktor3:3.4.0")
  implementation("androidx.glance:glance-appwidget:1.1.1")
  implementation("androidx.glance:glance-material3:1.1.1")
  implementation("com.materialkolor:material-kolor:2.1.1")
  implementation("org.jetbrains.compose.material3:material3:$composeVersion")
  implementation("org.jetbrains.compose.components:components-resources:$composeVersion")

  val ktorVersion = "3.4.0"

  implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")

  // HTTP client
  implementation("io.ktor:ktor-client-core:$ktorVersion")
  implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
  implementation("io.ktor:ktor-client-logging:$ktorVersion")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
}

android {
  compileSdk = (findProperty("android.compileSdk") as String).toInt()
  namespace = "app.octocon"

  sourceSets["main"].assets.srcDirs(layout.buildDirectory.dir("generated/shared-assets"))

  defaultConfig {
    applicationId = "app.octocon.OctoconApp"
    minSdk = (findProperty("android.minSdk") as String).toInt()
    targetSdk = (findProperty("android.targetSdk") as String).toInt()
    versionCode = (findProperty("android.versionCode") as String).toInt()
    versionName = findProperty("android.versionName") as String
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
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

  /*kotlinAndroid {
    // any Kotlin Android specific config
  }*/
}

dependencies {
  implementation("androidx.profileinstaller:profileinstaller:1.3.1")
}

val copyComposeResources by tasks.creating {
  val modules = listOf("shared" to "octoconapp.shared.generated.resources", "krop" to "octoconapp.krop.generated.resources")
  
  modules.forEach { (moduleName, targetFolder) ->
    val copyTask = tasks.create("copy${moduleName.capitalize()}Resources", Copy::class) {
      from(project(":$moduleName").layout.buildDirectory.dir("generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"))
      into(layout.buildDirectory.dir("generated/shared-assets/composeResources/$targetFolder"))
      dependsOn(":$moduleName:prepareComposeResourcesTaskForCommonMain")
    }
    dependsOn(copyTask)
  }
}

tasks.matching { it.name.contains("Assets") || it.name.contains("JavaRes") || it.name.contains("Resources") }.configureEach {
  if (this != copyComposeResources && !this.name.startsWith("copy") && !this.name.endsWith("Resources")) {
    dependsOn(copyComposeResources)
  }
}