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
    val decomposeVersion = "3.3.0-beta01"
    implementation(platform("com.google.firebase:firebase-bom:33.2.0"))
    implementation("com.google.firebase:firebase-messaging")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("androidx.browser:browser:1.8.0")
    implementation("com.arkivanov.decompose:decompose:$decomposeVersion")

    implementation("com.nimbusds:nimbus-jose-jwt:9.40")

    baselineProfile(project(":baselineprofile"))

    // Widget
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("io.coil-kt.coil3:coil:3.1.0")
    implementation("io.coil-kt.coil3:coil-network-ktor3:3.1.0")
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")
    compileOnly("com.materialkolor:material-kolor:2.0.2")
    // compileOnly(compose.material3)
    compileOnly("org.jetbrains.compose.material3:material3:1.8.0+dev2098")

    val ktorVersion = "3.0.3"

    compileOnly("org.jetbrains.kotlinx:kotlinx-datetime:0.6.1")
    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

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
}