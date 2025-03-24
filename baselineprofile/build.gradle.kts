plugins {
  kotlin("multiplatform")
  id("com.android.test")
  id("androidx.baselineprofile")
}

kotlin {
  androidTarget()
}

android {
  namespace = "app.octocon.baselineprofile"
  targetProjectPath = ":androidApp"

  compileSdk = (findProperty("android.compileSdk") as String).toInt()

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  defaultConfig {
    minSdk = (findProperty("android.minSdk") as String).toInt()
    targetSdk = (findProperty("android.targetSdk") as String).toInt()
    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  kotlin {
    jvmToolchain(17)
  }
}

// This is the configuration block for the Baseline Profile plugin.
// You can specify to run the generators on a managed devices or connected devices.
baselineProfile {
  useConnectedDevices = true
}

dependencies {
  implementation("androidx.test.ext:junit:1.1.5")
  implementation("androidx.test.espresso:espresso-core:3.5.1")
  implementation("androidx.test.uiautomator:uiautomator:2.2.0")
  implementation("androidx.benchmark:benchmark-macro-junit4:1.2.2")
}