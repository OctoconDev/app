import org.jetbrains.compose.desktop.application.dsl.TargetFormat

version = "1.3.0"

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
  id("dev.hydraulic.conveyor") version "1.13"
}

val composeVersion = findProperty("compose.version") as String

kotlin {
  jvm()
  jvmToolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
    vendor.set(JvmVendorSpec.JETBRAINS)
  }

  sourceSets {
    val decomposeVersion = "3.3.0-beta01"

    val jvmMain by getting {
      dependencies {
        implementation("org.jetbrains.compose.desktop:desktop-jvm:$composeVersion")
        implementation(project(":shared"))
        implementation("com.arkivanov.decompose:extensions-compose:$decomposeVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.1")
      }
    }
  }
}

compose.desktop {
  application {
    mainClass = "app.octocon.app.MainKt"

    nativeDistributions {
      targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
      packageName = "Octocon"
      packageVersion = "1.3.0"

      // val iconsRoot = project.file("desktop-icons")
      macOS {
        // iconFile.set(iconsRoot.resolve("icon-mac.icns"))
      }
      windows {
        // iconFile.set(iconsRoot.resolve("icon-windows.ico"))
        // menuGroup = "Compose Examples"
        // see https://wixtoolset.org/documentation/manual/v3/howtos/general/generate_guids.html
        // upgradeUuid = "18159995-d967-4CD2-8885-77BFA97CFA9F"
      }
      linux {
        // iconFile.set(iconsRoot.resolve("icon-linux.png"))
      }
    }

    buildTypes.release.proguard {
      version.set("7.4.0")

      obfuscate = false
      optimize = true

      configurationFiles.from(project.file("rules.pro"))
    }
  }
}

dependencies {
  // Use the configurations created by the Conveyor plugin to tell Gradle/Conveyor where to find the artifacts for each platform.
  linuxAmd64("org.jetbrains.compose.desktop:desktop-jvm-linux-x64:$composeVersion")
  macAmd64("org.jetbrains.compose.desktop:desktop-jvm-macos-x64:$composeVersion")
  macAarch64("org.jetbrains.compose.desktop:desktop-jvm-macos-arm64:$composeVersion")
  windowsAmd64("org.jetbrains.compose.desktop:desktop-jvm-windows-x64:$composeVersion")
}