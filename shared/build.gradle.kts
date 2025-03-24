@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  kotlin("native.cocoapods")
  id("com.android.library")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.mikepenz.aboutlibraries.plugin")
}

aboutLibraries {
  duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
}

kotlin {
  val essentyVersion = "2.5.0-beta01"
  val decomposeVersion = "3.3.0-beta01"

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
      baseName = "shared"
      isStatic = true
    }
  }*/

  iosArm64()
  iosSimulatorArm64()

  applyDefaultHierarchyTemplate()

  cocoapods {
    summary = "Octocon shared module"
    homepage = "https://octocon.app"
    version = "1.0"
    ios.deploymentTarget = "16.0"
    podfile = project.file("../iosApp/Podfile")
    framework {
      baseName = "shared"
      isStatic = true

      export("com.arkivanov.decompose:decompose:$decomposeVersion")
      export("com.arkivanov.essenty:lifecycle:$essentyVersion")
      export("com.arkivanov.essenty:state-keeper:$essentyVersion")
    }

    /*pod("ObjC-WebPImage") {
      extraOpts += listOf("-compiler-option", "-fmodules")
    }*/

    pod("TOCropViewController")
    pod("SDWebImage")
    pod("SDWebImageWebPCoder")
  }

  sourceSets {
    val ktorVersion = "3.0.3"

    wasmJs { browser() }

    val commonMain by getting {
      println(extra)
      // val markdownVersion = "0.31.0-rc01"
      val kamelVersion = "1.0.3"

      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")

        implementation(compose.runtime)
        implementation(compose.foundation)
        // implementation(compose.material3)
        implementation("org.jetbrains.compose.material3:material3:1.8.0+dev2098")
        implementation(compose.components.resources)
        implementation(compose.materialIconsExtended)

        api("com.arkivanov.essenty:lifecycle:$essentyVersion")
        implementation("com.arkivanov.essenty:lifecycle-coroutines:$essentyVersion")
        api("com.arkivanov.essenty:state-keeper:$essentyVersion")
        implementation("com.arkivanov.essenty:instance-keeper:$essentyVersion")
        implementation("com.arkivanov.essenty:back-handler:$essentyVersion")

        api("com.arkivanov.decompose:decompose:$decomposeVersion")
        implementation("com.arkivanov.decompose:extensions-compose-experimental:$decomposeVersion")

        // Color picker
        implementation(project(":color_picker"))

        // Image cropper
        implementation(project(":krop"))

        // HTTP client
        implementation("io.ktor:ktor-client-core:$ktorVersion")
        implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
        implementation("io.ktor:ktor-client-logging:$ktorVersion")

        // Image loading
        implementation("media.kamel:kamel-image:$kamelVersion")
        implementation("media.kamel:kamel-decoder-image-bitmap:$kamelVersion")

        // File picker
        implementation("io.github.vinceglb:filekit-compose:0.8.8")

        // Material 3 colors
        implementation("com.materialkolor:material-kolor:2.0.2")

        // Background blur
        implementation("dev.chrisbanes.haze:haze:1.0.2")

        // Crypto
        implementation(project.dependencies.platform("org.kotlincrypto.hash:bom:0.6.1"))
        implementation("org.kotlincrypto.hash:sha2")

        // Markdown
        // implementation("com.mikepenz:multiplatform-markdown-renderer:$markdownVersion")
        // implementation("com.mikepenz:multiplatform-markdown-renderer-m3:$markdownVersion")
        // TODO: Don't hoist
        implementation(project(":multiplatform-markdown-renderer"))
        implementation(project(":multiplatform-markdown-renderer-m3"))

        implementation("sh.calvin.reorderable:reorderable:2.4.3")

        implementation("com.mikepenz:aboutlibraries-core:11.6.0")
        implementation("com.mikepenz:aboutlibraries-compose-m3:11.6.0")

        // Phoenix channels
        implementation(project(":kotlix"))
      }
    }
    val androidMain by getting {
      dependencies {
        api("androidx.activity:activity-compose:1.10.0")
        api("androidx.appcompat:appcompat:1.7.0")
        api("androidx.core:core-ktx:1.15.0")
        implementation("androidx.core:core-splashscreen:1.0.1")
        api("androidx.security:security-crypto:1.1.0-alpha06")
        api("androidx.security:security-crypto-ktx:1.1.0-alpha06")

        implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
        // Phoenix channels
        // implementation("com.github.dsrees:JavaPhoenixClient:1.3.0")

        implementation("com.github.skydoves:cloudy:0.2.0")
      }
    }
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    val iosMain by getting {
      dependencies {
        implementation("io.ktor:ktor-client-darwin:$ktorVersion")
        implementation("com.liftric:kvault:1.12.0")
      }
    }

    val mobileMain by creating {
      dependsOn(commonMain)

      dependencies {
        implementation("dev.icerock.moko:permissions:0.18.1")
        implementation("dev.icerock.moko:permissions-compose:0.18.1")
      }
    }

    val wasmJsMain by getting {
      dependencies {
        implementation("io.ktor:ktor-client-js-wasm-js:$ktorVersion")

        implementation(npm("jose", "6.0.4"))
      }
    }

    androidMain.dependsOn(mobileMain)
    iosMain.dependsOn(mobileMain)

    all {
      languageSettings.optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
      languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
      languageSettings.optIn("androidx.compose.foundation.ExperimentalFoundationApi")
      languageSettings.optIn("androidx.compose.foundation.layout.ExperimentalLayoutApi")
      languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
    }
  }
}

android {
  compileSdk = (findProperty("android.compileSdk") as String).toInt()
  namespace = "app.octocon.common"

  sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
  sourceSets["main"].res.srcDirs("src/androidMain/res", "src/commonMain/res")
  sourceSets["main"].resources.srcDirs("src/commonMain/resources")

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