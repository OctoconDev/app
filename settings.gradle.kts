rootProject.name = "OctoconApp"

include(":shared")
include(":androidApp")
include(":webApp")
include(":color_picker")
include(":krop")
include(":kotlix")
include(":multiplatform-markdown-renderer")
include(":multiplatform-markdown-renderer-m3")
include(":baselineprofile")

pluginManagement {
  repositories {
    gradlePluginPortal()
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }

  plugins {
    val kotlinVersion = extra["kotlin.version"] as String
    val agpVersion = extra["agp.version"] as String
    val composeVersion = extra["compose.version"] as String

    kotlin("jvm").version(kotlinVersion)
    kotlin("multiplatform").version(kotlinVersion)
    kotlin("android").version(kotlinVersion)
    kotlin("plugin.serialization").version(kotlinVersion)
    kotlin("native.cocoapods").version(kotlinVersion)
    id("org.jetbrains.kotlin.plugin.compose").version(kotlinVersion)

    id("com.android.application").version(agpVersion)
    id("com.android.library").version(agpVersion)
    id("com.android.test").version(agpVersion)

    id("org.jetbrains.compose").version(composeVersion)
    id("androidx.baselineprofile").version("1.2.3")
    id("org.jetbrains.kotlin.android") version "2.1.0"
  }
}

plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version ("0.4.0")
}

dependencyResolutionManagement {
  @Suppress("UnstableApiUsage")
  repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
  }
}
