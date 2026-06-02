@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

plugins {
  kotlin("multiplatform")
  id("org.jetbrains.compose")
  id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
  wasmJs {
    browser {
      commonWebpackConfig {
        outputFileName = "octocon-app.js"
      }
    }
    useEsModules()
    binaries.executable()
  }

  sourceSets {
    val wasmJsMain by getting {
      dependencies {
        val composeVersion = findProperty("compose.version") as String
        implementation(project(":shared"))

        implementation("org.jetbrains.compose.ui:ui:$composeVersion")
        implementation("org.jetbrains.compose.foundation:foundation:$composeVersion")

        implementation("org.jetbrains.kotlinx:kotlinx-browser-wasm-js:0.5.0")
      }
    }
  }
}

// Generate a build-time precache list for the service worker by enumerating
// files under `build/processedResources/wasmJs/main` and replacing the
// `PRECACHE_URLS` array in the copied `service-worker.js` so the SW will
// pre-cache all emitted assets for offline use.

abstract class GenerateServiceWorkerPrecacheTask : DefaultTask() {

  @get:InputDirectory
  abstract val processedResourcesDir: DirectoryProperty

  @get:InputFile
  abstract val templateSourceFile: RegularFileProperty

  @get:OutputFile
  abstract val outputServiceWorkerFile: RegularFileProperty

  @TaskAction
  fun generate() {
    val processedDir = processedResourcesDir.get().asFile
    if (!processedDir.exists()) {
      println("[generatePrecache] no processedResources dir: ${processedDir.absolutePath}")
      return
    }

    val allowedExts = setOf("js","mjs","wasm","map","css","png","svg","ico","webmanifest","xml","json","jpg","jpeg","gif","webp","ttf","otf")

    val files = processedDir.walkTopDown()
      .filter { it.isFile }
      .map { 
        "/" + it.relativeTo(processedDir).path.replace(File.separatorChar, '/')
      }
      .filter { path ->
        val ext = path.substringAfterLast('.', "").lowercase()
        ext in allowedExts
      }
      .sorted()
      .distinct()
      .toList()

    if (files.isEmpty()) {
      println("[generatePrecache] no matching files found, leaving existing PRECACHE_URLS unchanged")
      return
    }

    val urlsArrayText = files.joinToString(",\n") { "  \"$it\"" }

    val swFile = outputServiceWorkerFile.get().asFile

    val templateFile = templateSourceFile.get().asFile
    val content = when {
      templateFile.exists() -> templateFile.readText(Charsets.UTF_8)
      swFile.exists() -> swFile.readText(Charsets.UTF_8)
      else -> {
        println("[generatePrecache] no service-worker template found, skipping")
        return
      }
    }

    val regex = Regex("""const PRECACHE_URLS = \[([\s\S]*?)\];""")
    val newContent = if (regex.containsMatchIn(content)) {
      content.replace(regex, "const PRECACHE_URLS = [\n$urlsArrayText\n];")
    } else {
      content.replaceFirst("const CACHE_NAME = 'octocon-app-cache-v1';", "const CACHE_NAME = 'octocon-app-cache-v1';\nconst PRECACHE_URLS = [\n$urlsArrayText\n];")
    }

    // Ensure output directory exists and write atomically
    swFile.parentFile.mkdirs()
    val tmp = File(swFile.parentFile, ".service-worker.js.tmp")
    tmp.writeText(newContent, Charsets.UTF_8)
    if (!tmp.renameTo(swFile)) {
      tmp.copyTo(swFile, overwrite = true)
      tmp.delete()
    }
    println("[generatePrecache] updated ${swFile.absolutePath} with ${files.size} entries")
  }
}

val generateServiceWorkerPrecache = tasks.register<GenerateServiceWorkerPrecacheTask>("generateServiceWorkerPrecache") {
  processedResourcesDir.set(layout.buildDirectory.dir("processedResources/wasmJs/main"))
  templateSourceFile.set(layout.projectDirectory.file("src/wasmJsMain/resources/service-worker.js"))
  outputServiceWorkerFile.set(layout.buildDirectory.file("processedResources/wasmJs/main/service-worker.js"))
}


// Run precache generation after webpack builds (production and development webpack tasks).
tasks.matching { it.name == "wasmJsBrowserProductionWebpack" || it.name == "wasmJsBrowserDevelopmentWebpack" }
  .configureEach {
    finalizedBy(generateServiceWorkerPrecache)
  }

// Ensure the generator runs after resource copy tasks and before `wasmJsBrowserDevelopmentRun`.
generateServiceWorkerPrecache.configure {
  dependsOn(tasks.matching { it.name.endsWith("ProcessResources") })
}

tasks.matching { it.name == "wasmJsBrowserDevelopmentRun" || it.name == "wasmJsBrowserDistribution" }
  .configureEach {
    dependsOn(generateServiceWorkerPrecache)
  }

// The development executable compile-sync task reads the generated service-worker
// file. Declare an explicit dependency so Gradle's validation doesn't complain
// about the implicit input/output relationship.
tasks.matching { it.name == "wasmJsDevelopmentExecutableCompileSync" }
  .configureEach {
    dependsOn(generateServiceWorkerPrecache)
  }

// NOTE: do not create a compile-sync -> generateServiceWorkerPrecache dependency
// because it can produce a circular dependency with webpack tasks. The
// generator is instead wired to run after webpack via `finalizedBy` above and
// should depend on copy tasks if additional ordering is required.

// Prevent Kotlin's generated process resources Copy task from copying the
// source `service-worker.js` into processedResources, which would overwrite
// the file generated by `generateServiceWorkerPrecache`.
// The Kotlin plugin actually registers the task as `wasmJsProcessResources`.
tasks.matching { it.name.endsWith("ProcessResources") }
  .configureEach {
    if (this is org.gradle.api.tasks.Copy) {
      exclude("service-worker.js")
    }
  }