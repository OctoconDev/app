# [Octocon](https://octocon.app) frontend

***WIP***

This repository contains the source code for Octocon's entire frontend, including our mobile app and our website.

It is recommended to open this repository as an Android Studio project in an environment that has followed the
[Kotlin Multiplatform setup guide](https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-setup.html).
It is further recommended to open the project on a macOS system; otherwise, you will only be able to build the app's
Android and web targets.

## Architecture & project structure

The Octocon app is built as a [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html) project with the
[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) UI framework. As such, Octocon is largely
written with Android development techniques, and even compiles to native Jetpack Compose code on the Android platform.

As much code as possible is written in common (cross-platform) Kotlin code, but it's sometimes necessary to write
platform-specific code to utilize native APIs. In this case, we take two approaches:
- Implementing this logic in platform-specific Kotlin under a sourceset with `expect/actual` (**heavily preferred!**)
- Utilizing native code, and injecting data/logic to the shared context if necessary (e.g. by implementing a Kotlin interface with Swift)

Instead of spending development time on techniques such as optimistic UI, Octocon uses
[Phoenix channels](https://hexdocs.pm/phoenix/channels.html) to receive real-time updates on the app's data through
a persistent, low-latency WebSocket connection.
The server acts as the authoritative source of truth for data mutation under all circumstances.

### Root

The root folder contains a Gradle project designed to centralize build dependencies across all of Octocon's submodules.
Development of new submodules should be done in a designated folder and marked as part of the project in
`settings.gradle.kts` (e.g. `include(":shared")`).

### Shared ([`/shared`](/shared))

The vast majority of Octocon's code is written in common Kotlin contained in this submodule (`src/commonMain`).
This includes:
- `ui/model` - The [Decompose](https://github.com/arkivanov/Decompose) component model of the app's navigation structure and core business logic.
- `ui/compose` - The Compose Multiplatform implementation of the Decompose model; **most code is here!**
- `api` - Kotlin implementations of our API's data model and endpoints, designed for use with `kotlinx-serialization`.

Efforts are underway to isolate `ui/model` as its own Gradle submodule with minimal dependencies and hoist as much business logic as possible there.
This will allow for alternative frontends (such as a SwiftUI implementation) to be written while keeping most business logic in Kotlin and deduplicated.

### Android ([`/androidApp`](/androidApp))



### iOS ([`/androidApp`](/androidApp))



### Web ([`/webApp`](/webApp))

## Website ([`/website`](/website))

In addition to the Octocon app, this project also contains the source code for the [Octocon website](https://octocon.app).
It is built with TypeScript, Vue, Nuxt 3, and UnoCSS. Content is
authored in Markdown for use with `@nuxt/content`.

The Nuxt application embeds the Octocon web app in the form of a WebAssembly binary, which **must** be built through the
` webApp:wasmJsBrowserDistribution` Gradle task and copied to the `/public` folder before deployment. This can be
done with `pnpm run app:build:unix` or `pnpm run app:build:windows`, depending on your platform.

### Setup

It is recommended to use `pnpm` as a package manager; `pnpm` is available in modern
versions of Node by default with Corepack by running `corepack enable`.

Install the dependencies first:

```bash
pnpm install
```

#### Development Server

Start the development server on `http://localhost:3000`:

```bash
pnpm run dev
```

#### Production

Build the application for production:

```bash
pnpm run build
```

Locally preview the production build:

```bash
pnpm run preview
```
