plugins {
  // This is necessary to avoid the plugins to be loaded multiple times in each subproject's classloader.
  kotlin("multiplatform").apply(false)
  kotlin("native.cocoapods").apply(false)
  id("com.android.library").apply(false)
  id("org.jetbrains.compose").apply(false)
  id("org.jetbrains.kotlin.plugin.compose").apply(false)
  id("com.android.application").apply(false)
  id("com.google.gms.google-services").version("4.4.0").apply(false)
  id("com.mikepenz.aboutlibraries.plugin").version("11.2.1").apply(false)
}
