<template>
  <div class="relative">
    <div class="absolute z--1 w-screen h-screen grid place-items-center">
      <div class="flex flex-col gap-4 items-center text-center">
        <NuxtPicture
          src="/img/octocon-logo.png"
          alt="Octocon logo"
          format="avif,webp"
          width="96"
          height="96"
          sizes="256"
          quality="90"
        />
        <div class="text-3xl font-display text-gray-300">
          Loading Octocon app...
        </div>
        <div class="font-body text-xl text-gray-400 font-medium">
          This may take a minute!
        </div>
      </div>
    </div>
    <div v-if="error != null" class="absolute z-99 w-screen h-screen">
      <div class="bg-black/60 h-full w-full grid place-items-center">
        <div
          class="relative bg-gray-950 p-8 rounded-xl max-w-xl flex flex-col gap-4"
        >
          <button
            class="absolute top-4 right-4 p-2 rounded-xl text-lg bg-gray-800 backdrop-blur-sm"
            @click="error = null"
          >
            <UnoIcon
              class="text-gray-400"
              :class="'i-material-symbols-close-rounded'"
            />
          </button>
          <h1 class="text-3xl font-display text-gray-300 text-center">
            🤦 Whoops!
          </h1>
          <p class="text-lg text-gray-400">
            An error occurred while running the app. Please report this to the
            Octocon developers
            <NuxtLink to="https://octocon.app/discord" class="text-violet-400"
              >on Discord</NuxtLink
            >
            !
          </p>
          <p class="text-lg text-gray-400">
            You can click the "X" above to ignore this error, but the app likely
            won't work correctly.
          </p>
          <p class="text-sm text-gray-500 text-center font-semibold">
            {{ error }}
          </p>
        </div>
      </div>
    </div>
    <canvas class="z-98 w-screen h-screen" id="ComposeTarget" />
  </div>
</template>
<script setup lang="ts">
const error = ref<Error | null>(null)
onMounted(() => {
  // Ugly hack to get Kotlin/Wasm to not implode because Nuxt simulates `window.process`
  if (window.process) {
    window.process.release = {
      name: 'meow'
    }
  }

  // Hook Vue into uncaught Wasm errors
  addEventListener('error', (event) => {
    error.value = event.error
    console.log(event)
  })

  // Load app Wasm script outside of the Vue context
  const script = document.createElement('script')
  script.src = '/priv/app/octocon-app.js'
  script.async = true
  script.defer = true
  document.body.appendChild(script)

  window.lastWasError = false
  // Repeatedly warn the user in the console not to paste anything there
  setInterval(() => {
    const fun = window.lastWasError ? console.warn : console.error
    fun(
      `⚠️ DO NOT copy or paste anything here! If someone told you to access this screen, they are trying to scam you and/or gain access to your Octocon account. ${Math.random()}`
    )
    window.lastWasError = !window.lastWasError
  }, 3000)
})

declare namespace window {
  let lastWasError: boolean
}

definePageMeta({
  layout: 'app'
})
</script>
