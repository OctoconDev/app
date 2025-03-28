# [Octocon](https://octocon.app) frontend

This repository contains the source code for Octocon's entire frontend, including our mobile app and our website.

It is recommended to open this repository as an Android Studio project in an environment that has followed the
[Kotlin Multiplatform setup guide](https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-setup.html).
It is further recommended to do this on a macOS system; otherwise, you will only be able to build the app's
Android and web targets.

If you'd simply like instructions to run a development build of Octocon, scroll down to the "Development" page for
the corresponding platform you'd like to test (marked with a 🛠). Much of this README serves a dual-purpose as
an explanation of our frontend tech stack.

## 🏗 Architecture & shared code

The Octocon app is built as a [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) project with the
[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) UI framework. As such, Octocon is largely
written with Android development techniques, and even compiles to native Jetpack Compose code on the Android platform.

As much code as possible is written in common (cross-platform) Kotlin code, but it's sometimes necessary to write
platform-specific code to utilize native APIs. In this case, we take two approaches:
- Implementing this logic in platform-specific Kotlin under a sourceset with `expect/actual` (**heavily preferred!**)
  - Kotlin/JVM for Android, Kotlin/Native with Objective-C interop for iOS, Kotlin/Wasm for web
- Utilizing native code, and injecting data/logic to the shared context if necessary
  - This could look like e.g. implementing a Kotlin interface with Swift and passing it to a Kotlin function

Instead of spending development time on techniques such as optimistic UI, Octocon uses
[Phoenix channels](https://hexdocs.pm/phoenix/channels.html) to receive real-time updates on the app's data through
a persistent, low-latency WebSocket connection.
The server acts as the authoritative source of truth for data mutation under all circumstances.

### Root

The root directory contains a Gradle project designed to centralize build dependencies across all of Octocon's submodules.
Development of new submodules should be done in a designated directory and marked as part of the project in
`settings.gradle.kts` (e.g. `include(":shared")`).

### Shared
> ([`/shared`](/shared))

The vast majority of Octocon's code is written in common Kotlin contained in this submodule (`src/commonMain`).
This includes:
- `ui/model` - The [Decompose](https://github.com/arkivanov/Decompose) component model of the app's navigation structure and core business logic.
- `ui/compose` - The Compose Multiplatform implementation of the Decompose model; **most code is here!**
- `api` - Kotlin implementations of our API's data models, endpoints, and Phoenix channel messages, designed for use with `kotlinx-serialization`.

Efforts are underway to isolate `ui/model` as its own Gradle submodule with minimal dependencies and hoist as much business logic as possible there.
This will allow for alternative frontends (such as a SwiftUI implementation) to be written while keeping most business logic in Kotlin and deduplicated.

## Android
> ([`/androidApp`](/androidApp))

This directory contains the wrapper for the Android app. It largely consists of the following components:
- Initializing a main `Activity` and the root Decompose component context, after which control is handed over to `:shared` through `RootScreen`.
- Registering an Android widget with the operating system (built with Jetpack Glance)
- Minimal implementations of code that is inconvenient to colocate in the `androidMain` sourceset of `:shared` (e.g. some crypto, `PlatformUtilities`, and `SharedPreferences`)

### 🛠 Development

Import the **root** Gradle project; Android Studio will detect `:androidApp` and allow you to create a run configuration for it. It is recommended to test on a physical device
instead of an emulator if possible.

#### Baseline profile ([`/baselineprofile`](/baselineprofile))

The `:baselineprofile` submodule contains the code necessary to generate [Android baseline profiles](https://developer.android.com/topic/performance/baselineprofiles/overview)
for `:androidApp` with Macrobenchmark. This greatly improves the performance of the app upon first launch and when cold booting.

## iOS
> ([`/iosApp`](/iosApp))

This directory contains the wrapper for the iOS app. It largely consists of the following components:
- CocoaPods project configuration
- Initializing a `UIApplication`, an `AppDelegate`, and the root Decompose component context, after which control is handed over to `:shared` through `RootScreen` and a Compose Multiplatform `UIViewController`.
- Registering an iOS widget with the operating system (built with WidgetKit and SwiftUI)
  - Minimal Swift implementations of Octocon's relevant data models in Swift, for use with the widget's isolated networking layer
- Minimal implementations of code that must be written in Swift (usually to interface with libraries that don't provide an Objective-C ABI for Kotlin/Native, like CryptoKit)

Some configuration files of the Xcode project are omitted from this repository for security reasons.

### 🛠 Development

Running a development build of Octocon for iOS is much more complex than its other supported platforms. Generally, you can follow the following steps:
- Install the Xcode Command Line Tools, modern Ruby, CocoaPods, and a JDK
- Validate your environment with [KDoctor](https://github.com/Kotlin/kdoctor)
- Import the Xcode workspace in `iosApp`
- Attempt to run a debug build from within Xcode, ***or*** create a run configuration for `iosApp` in Android Studio (the latter is more finicky)
- Pray and perform a blood sacrifice to Tim Cook.

It is recommended to test on a physical device instead of an emulator if possible. Xcode's iOS emulator is known to have issues with features such as
app links.

## Web
> ([`/webApp`](/webApp))

This directory contains the wrapper for the experimental WebAssembly ("Wasm") build of Octocon. This allows Octocon to be run in any web browser that supports modern Wasm
features, namely the [Wasm GC (garbage collection) specification](https://github.com/WebAssembly/gc). It currently consists solely of a small amount of glue code to
initialize the root Decompose component context and get `RootScreen` running in a Compose-enabled `<canvas>` element.

### 🛠 Development

The web app can be run directly from the built `index.html` through the `:webApp:wasmJsBrowserDevelopmentRun` Gradle task, but it is recommended to hoist it into another
environment, such as the Octocon website. Instructions for this are [detailed below](#website).

Octocon for Wasm should run on any web page with a `<canvas id="ComposeTarget">` and the `octocon-app.js` script generated by Gradle imported. An example of this can be
seen in [/website/pages/app.vue](website/pages/app.vue#L62).

## Website

> ([`/website`](/website))

In addition to the Octocon app, this repository also contains the source code for the [Octocon website](https://octocon.app).
It is built with TypeScript, Vue, Nuxt 3, and UnoCSS. Content is
authored in Markdown for use with `@nuxt/content`.

It is recommended to open this directory as a *separate* project in a web development-focused IDE with support for Vue,
such as WebStorm or Visual Studio Code with Vue language support extension (previously "Volar"). However, if you are only
syncing the web app's assets, it is sufficient to run `pnpm` tasks directly from Android Studio's terminal.

The Nuxt application embeds the Octocon web app in the form of a WebAssembly binary, which **must** be built through the
` webApp:wasmJsBrowserDistribution` Gradle task and copied to the `/public` directory before deployment. This can be
done with `pnpm run app:build:unix` or `pnpm run app:build:windows`, depending on your platform.

> This will likely be automated in the near future through CI/CD (likely GitHub Actions).

### Setup

It is recommended to use `pnpm` as a package manager; `pnpm` is available in modern
versions of Node by default with Corepack by running `corepack enable`.

Be sure to install the dependencies first:

```bash
pnpm install
```

### 🛠 Development

Start the development server on `http://localhost:3000`:

```bash
pnpm run dev
```

### Building for production

Build the web app with Gradle and copy it to the website's assets:

```bash
pnpm run app:build:unix
# or `pnpm run app:build:windows` on Windows
```

Build the website for production:

```bash
pnpm run build
```

Locally preview the production build:

```bash
pnpm run preview
```
