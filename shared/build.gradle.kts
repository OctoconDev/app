@file:OptIn(ExperimentalWasmDsl::class)

import java.io.File
import org.gradle.kotlin.dsl.support.serviceOf
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl

plugins {
  kotlin("multiplatform")
  kotlin("plugin.serialization")
  kotlin("native.cocoapods")
  id("com.android.kotlin.multiplatform.library")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
  id("com.mikepenz.aboutlibraries.plugin")
}

aboutLibraries {
  duplicationMode = com.mikepenz.aboutlibraries.plugin.DuplicateMode.MERGE
}

kotlin {
  val essentyVersion = "2.5.0"
  val decomposeVersion = "3.4.0"

  targets.named<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget>("android") {
    namespace = "app.octocon.common"
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    minSdk = (findProperty("android.minSdk") as String).toInt()

    withDeviceTestBuilder {
      sourceSetTreeName = "test"
    }.configure {
      instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
  }

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

  jvm("desktop") {
    @OptIn(ExperimentalKotlinGradlePluginApi::class)
    val main = compilations.getByName("main")
    compilations.create("integrationTest") {
      associateWith(main)
    }
  }

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
    val ktorVersion = "3.4.0"

    wasmJs { browser() }

    val commonMain by getting {
      println(extra)
      // val markdownVersion = "0.31.0-rc01"
      val kamelVersion = "1.0.9"

      dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
        implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.7.1")

        val composeVersion = findProperty("compose.version") as String
        val materialIconsVersion = findProperty("material.icons.version") as String
        val material3Version = findProperty("compose.material3.version") as String
        implementation("org.jetbrains.compose.runtime:runtime:$composeVersion")
        implementation("org.jetbrains.compose.foundation:foundation:$composeVersion")
        compileOnly("org.jetbrains.compose.material3:material3:$material3Version")
        implementation("org.jetbrains.compose.components:components-resources:$composeVersion")
        implementation("org.jetbrains.compose.material:material-icons-core:$materialIconsVersion")
        implementation("org.jetbrains.compose.material:material-icons-extended:$materialIconsVersion")

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
        implementation("io.github.vinceglb:filekit-dialogs-compose:0.10.0")

        // Material 3 colors
        implementation("com.materialkolor:material-kolor:2.1.1")

        // Background blur
        implementation("dev.chrisbanes.haze:haze:1.7.2")

        // Crypto
        implementation(project.dependencies.platform("org.kotlincrypto.hash:bom:0.6.1"))
        implementation("org.kotlincrypto.hash:sha2")

        // Markdown
        // implementation("com.mikepenz:multiplatform-markdown-renderer:$markdownVersion")
        // implementation("com.mikepenz:multiplatform-markdown-renderer-m3:$markdownVersion")
        // TODO: Don't hoist
        implementation(project(":multiplatform-markdown-renderer"))
        implementation(project(":multiplatform-markdown-renderer-m3"))

        implementation("sh.calvin.reorderable:reorderable:3.0.0")

        implementation("com.mikepenz:aboutlibraries-core:13.2.1")
        implementation("com.mikepenz:aboutlibraries-compose-m3:13.2.1")

        // Phoenix channels
        implementation(project(":kotlix"))
      }
    }
    val androidMain by getting {
      dependencies {
        implementation("androidx.core:core-ktx:1.17.0")
        api("androidx.activity:activity-compose:1.12.4")
        api("androidx.appcompat:appcompat:1.7.1")
        api("androidx.core:core-ktx:1.17.0")
        implementation("androidx.core:core-splashscreen:1.2.0")
        api("androidx.security:security-crypto:1.1.0")
        api("androidx.security:security-crypto-ktx:1.1.0")

        implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
        // Phoenix channels
        // implementation("com.github.dsrees:JavaPhoenixClient:1.3.0")

        implementation("com.github.skydoves:cloudy:0.5.0")
      }
    }
    val iosArm64Main by getting
    val iosSimulatorArm64Main by getting
    val iosMain by getting {
      dependencies {
        implementation("io.ktor:ktor-client-darwin:$ktorVersion")
        implementation("com.liftric:kvault:1.12.0")
        api("androidx.performance:performance-annotation:1.0.0-alpha01")
      }
    }

    val mobileMain by creating {
      dependsOn(commonMain)

      dependencies {
        implementation("dev.icerock.moko:permissions:0.20.1")
        implementation("dev.icerock.moko:permissions-notifications:0.20.1")
        implementation("dev.icerock.moko:permissions-compose:0.20.1")
      }
    }

    androidMain.dependsOn(mobileMain)
    iosMain.dependsOn(mobileMain)

    val wasmJsMain by getting {
      dependencies {
        implementation("io.ktor:ktor-client-js-wasm-js:$ktorVersion")

        implementation(npm("jose", "6.0.4"))
      }
    }

    val desktopMain by getting {
      dependencies {
        implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
        implementation("com.nimbusds:nimbus-jose-jwt:9.47")
      }
    }

    val commonTest by getting {
      dependencies {
        val composeVersion = findProperty("compose.version") as String
        val material3Version = findProperty("compose.material3.version") as String

        implementation(kotlin("test"))
        implementation("org.jetbrains.compose.ui:ui-test:$composeVersion")
        implementation("org.jetbrains.compose.material3:material3:$material3Version")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
      }
    }

    val androidDeviceTest by getting {
      dependencies {
        val composeVersion = findProperty("compose.version") as String

        implementation("androidx.compose.ui:ui-test-junit4-android:$composeVersion")
        implementation("androidx.compose.ui:ui-test-manifest:$composeVersion")
        implementation("androidx.test:core:1.6.1")
      }
    }

    val desktopTest by getting {
      dependencies {
        implementation(compose.desktop.uiTestJUnit4)
        implementation(compose.desktop.currentOs)
      }
    }

    val desktopIntegrationTest by getting {
      // No explicit dependsOn(desktopMain): the JVM compilation's `associateWith(main)`
      // already arranges visibility/classpath for us. Setting `dependsOn` here would
      // double-link the source set and trigger a KGP warning.
      dependencies {
        implementation(kotlin("test"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
        implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
        implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
        implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
        // 1.21.4 is the first release whose bundled docker-java client speaks
        // Docker Engine API v1.44+ — older versions (incl. 1.20.x) fail to
        // discover modern daemons (Docker 29+, Docker Desktop 4.52+) with
        // `client version 1.32 is too old`.
        implementation("org.testcontainers:testcontainers:1.21.4")
        implementation("org.testcontainers:junit-jupiter:1.21.4")
        implementation("org.junit.jupiter:junit-jupiter:5.11.4")
        // Required on the runtime classpath by Gradle 8+ for `useJUnitPlatform()`.
        // The `junit-jupiter` aggregator does NOT pull this in.
        runtimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
        // Testcontainers + ktor-client both log via SLF4J. Without a binding we
        // get a `No SLF4J providers were found` warning AND, more importantly,
        // every Testcontainers diagnostic — including the docker-daemon
        // discovery probe failures — is swallowed. slf4j-simple keeps the log
        // surface dependency-free and prints to stderr.
        runtimeOnly("org.slf4j:slf4j-simple:2.0.16")
      }
    }

    all {
      languageSettings.optIn("org.jetbrains.compose.resources.ExperimentalResourceApi")
      languageSettings.optIn("kotlinx.serialization.ExperimentalSerializationApi")
      languageSettings.optIn("androidx.compose.foundation.ExperimentalFoundationApi")
      languageSettings.optIn("androidx.compose.foundation.layout.ExperimentalLayoutApi")
      languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3Api")
      languageSettings.optIn("androidx.compose.material3.ExperimentalMaterial3ExpressiveApi")
    }
  }
}

compose.resources {
  packageOfResClass = "octoconapp.shared.resources"
  publicResClass = true
}

// Workaround: with Compose Multiplatform 1.11.x + the new
// `com.android.kotlin.multiplatform.library` plugin, the compose-resources plugin's
// `configureGeneratedAndroidComponentAssets` step doesn't successfully call
// `addGeneratedSourceDirectory(...)` for KMP-Android-Library variants. Three symptoms:
//  1. The auto-created `copy<Variant>ComposeResourcesToAndroidAssets` tasks never get
//     their `outputDirectory` wired, so Gradle's property-validation step blocks the
//     build before any of the downstream work can run.
//  2. Their `from` is empty because the variant→compilation lookup misses, so even if
//     they ran they'd copy nothing.
//  3. Their output isn't registered as a variant assets source, so even if they
//     produced files, `mergeAssets` wouldn't see them. The test APK ships with no
//     `composeResources/...` and every `Res.string.*.compose` call throws
//     `MissingResourceException` at runtime.
//
// Fix: drive the auto-generated copy tasks ourselves — set their `outputDirectory`
// and `from` via reflection (the task class is `internal`), then wire each Android
// variant's `mergeAssets` to depend on the matching copy task and treat its output
// as a static asset source.
val composeResourcesPackage = "octoconapp.shared.resources"
val composeResourcesPrepared =
  layout.buildDirectory.dir(
    "generated/compose/resourceGenerator/preparedResources/commonMain/composeResources"
  )

fun composeResourcesCopyOutputDir(variantName: String): Provider<Directory> =
  layout.buildDirectory.dir("workaround/composeAndroidAssets/$variantName")

afterEvaluate {
  tasks.matching {
    it.name.startsWith("copyAndroid") &&
      it.name.endsWith("ComposeResourcesToAndroidAssets")
  }.configureEach {
    dependsOn(tasks.named("prepareComposeResourcesTaskForCommonMain"))
    val taskName = name
    val variantName = taskName
      .removePrefix("copy")
      .removeSuffix("ComposeResourcesToAndroidAssets")
      .replaceFirstChar { it.lowercase() }
    val outputDir = composeResourcesCopyOutputDir(variantName)
    val outDirProp = this::class.members.firstOrNull { it.name == "outputDirectory" }
      ?: error("CopyResourcesToAndroidAssetsTask no longer exposes `outputDirectory` — the workaround needs updating")
    val fromProp = this::class.members.firstOrNull { it.name == "from" }
      ?: error("CopyResourcesToAndroidAssetsTask no longer exposes `from` — the workaround needs updating")
    val relativeProp = this::class.members.firstOrNull { it.name == "relativeResourcePlacement" }
      ?: error("CopyResourcesToAndroidAssetsTask no longer exposes `relativeResourcePlacement` — the workaround needs updating")
    @Suppress("UNCHECKED_CAST")
    (outDirProp.call(this) as org.gradle.api.file.DirectoryProperty).set(outputDir)
    @Suppress("UNCHECKED_CAST")
    (fromProp.call(this) as org.gradle.api.provider.Property<org.gradle.api.file.FileCollection>)
      .set(files(composeResourcesPrepared))
    @Suppress("UNCHECKED_CAST")
    (relativeProp.call(this) as org.gradle.api.provider.Property<File>)
      .set(File("composeResources/$composeResourcesPackage"))
  }

  // Inject the copied compose-resources directory into each Android variant's
  // mergeAssets output. addStaticSourceDirectory rejects paths inside build/, so we
  // hook the merge task: depend on the matching copy task, advertise our directory
  // as an input, then copy our files into the merged-assets output as the very last
  // step. The downstream compress/package steps pick the files up automatically.
  val fileSystemOps = project.serviceOf<org.gradle.api.file.FileSystemOperations>()
  tasks.matching { it.name.startsWith("mergeAndroid") && it.name.endsWith("Assets") }
    .configureEach {
      val mergeTaskName = name
      val variantName = mergeTaskName
        .removePrefix("merge")
        .removeSuffix("Assets")
        .replaceFirstChar { it.lowercase() }
      val copyTaskName = "copy${variantName.replaceFirstChar { it.uppercase() }}ComposeResourcesToAndroidAssets"
      val copyTask = tasks.findByName(copyTaskName) ?: return@configureEach
      val workaroundDir = composeResourcesCopyOutputDir(variantName)
      dependsOn(copyTask)
      inputs.dir(workaroundDir)
        .withPropertyName("composeAndroidAssetsWorkaround")
        .withPathSensitivity(org.gradle.api.tasks.PathSensitivity.RELATIVE)
      val workaroundDirFile = workaroundDir.get().asFile
      doLast {
        val mergedAssetsDir = outputs.files.firstOrNull { it.isDirectory } ?: return@doLast
        fileSystemOps.copy {
          from(workaroundDirFile)
          into(mergedAssetsDir)
        }
      }
    }
}

// Stand-alone Test task driving the desktopIntegrationTest compilation. Not
// wired into `:check`; explicit invocation only. The compilation is associated
// with `desktopMain`, so its tests can reach `internal` symbols on the JVM
// target (e.g. `ApiInterfaceImpl`) without us having to widen production
// visibility.
@OptIn(ExperimentalKotlinGradlePluginApi::class)
val integrationCompilation =
  kotlin.jvm("desktop").compilations.getByName("integrationTest")

tasks.register<Test>("desktopIntegrationTest") {
  group = "verification"
  description = "Runs JVM integration tests against an in-memory Octocon backend."
  testClassesDirs = integrationCompilation.output.classesDirs
  classpath =
    integrationCompilation.runtimeDependencyFiles + integrationCompilation.output.allOutputs
  useJUnitPlatform()
  // Forward the image override knob so `-Doctocon.backend.image=...` works.
  systemProperty("octocon.backend.image", System.getProperty("octocon.backend.image", ""))
  // Forward Docker / Testcontainers env vars to the worker JVM. Gradle does
  // NOT inherit shell env into test workers by default, so without this the
  // Testcontainers DockerClientProviderStrategy can't find the daemon — even
  // when `docker info` from the launching shell works fine. Only forwards
  // each variable when it is actually set on the launching env, so machines
  // that rely on the default `unix:///var/run/docker.sock` aren't affected.
  listOf(
    "DOCKER_HOST",
    "DOCKER_TLS_VERIFY",
    "DOCKER_CERT_PATH",
    "TESTCONTAINERS_HOST_OVERRIDE",
    "TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE",
    "TESTCONTAINERS_RYUK_DISABLED",
  ).forEach { name ->
    System.getenv(name)?.let { environment(name, it) }
  }
  testLogging {
    events("passed", "failed", "skipped", "standardError")
    showStandardStreams = true
    exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
  }
}