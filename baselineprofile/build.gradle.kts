plugins {
  id("com.android.test")
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

  /*kotlinAndroid {
  }*/
}

dependencies {
  implementation("androidx.test.ext:junit:1.3.0")
  implementation("androidx.test.espresso:espresso-core:3.7.0")
  implementation("androidx.test.uiautomator:uiautomator:2.3.0")
  implementation("androidx.benchmark:benchmark-macro-junit4:1.4.1")
}