<template>
  <div
    class="relative bg-gradient-to-b from-gray-950 to-gray-900 min-h-screen flex flex-col items-center"
  >
    <!--    <div class="inset-0 absolute w-screen pointer-events-none">
      <img
        src="/img/noise.svg"
        class="w-full h-full object-cover opacity-[2%]"
      />
    </div>-->
    <div
      class="container flex flex-col z-99 items-center justify-center h-screen px-8 py-24 sm:py-32 md:py-40"
    >
      <div class="relative h-[64px] w-[64px]">
        <NuxtPicture
          src="/img/octocon-logo.png"
          alt="Octocon logo"
          format="avif,webp"
          width="64"
          height="64"
          sizes="128"
          quality="90"
        />
        <div
          class="rounded-full octo-gradient absolute inset--50% md:inset--100% z--1 blur-lg"
        />
      </div>
      <h1
        class="font-bold font-display text-4xl pt-4 pb-8 text-gray-200"
      >
        Octocon is shutting down
      </h1>
      <OctoCard v-if="token == null">
    <p class="lg:text-lg text-gray-300 text-center">
      Click on the platform you used to access Octocon to export your data.
    </p>
    <div class="flex flex-col-reverse lg:flex-row justify-center gap-4">
      <NuxtLink
        to="https://api.octocon.app/auth/export/discord"
        class="block p-0.5 rounded-lg bg-gradient-to-br from-sky-400 to-violet-400"
      >
        <div
          class="bg-gradient-to-br from-sky-600 text-center to-violet-600 transition-all hover:brightness-110 block text-gray-100 font-semibold font-display text-lg px-4 py-2 rounded-lg"
        >
          Discord
        </div>
      </NuxtLink>
      <NuxtLink
        to="https://api.octocon.app/auth/export/google"
        class="block p-0.5 rounded-lg bg-gradient-to-br from-sky-400 to-violet-400"
      >
        <div
          class="bg-gradient-to-br from-sky-600 text-center to-violet-600 transition-all hover:brightness-110 block text-gray-100 font-semibold font-display text-lg px-4 py-2 rounded-lg"
        >
          Google
        </div>
      </NuxtLink>
      <NuxtLink
        to="https://api.octocon.app/auth/export/apple"
        class="block p-0.5 rounded-lg bg-gradient-to-br from-sky-400 to-violet-400"
      >
        <div
          class="bg-gradient-to-br from-sky-600 text-center to-violet-600 transition-all hover:brightness-110 block text-gray-100 font-semibold font-display text-lg px-4 py-2 rounded-lg"
        >
          Apple
        </div>
      </NuxtLink>
    </div>
  </OctoCard>
  <OctoCard v-else>
    <p class="lg:text-lg text-gray-300 text-center">
      Choose which export type you would like to perform. A "Full" export includes all of your data, while a "PluralKit" export includes less but works with all PluralKit-compatible platforms. <strong>You may only perform one export every 4 hours.</strong>
    </p>
    <div class="flex flex-col-reverse lg:flex-row justify-center gap-4">
      <button
        @click="handleFull"
        class="block p-0.5 rounded-lg bg-gradient-to-br from-sky-400 to-violet-400"
      >
        <div
          class="bg-gradient-to-br from-sky-600 text-center to-violet-600 transition-all hover:brightness-110 block text-gray-100 font-semibold font-display text-lg px-4 py-2 rounded-lg"
        >
          Full
        </div>
      </button>
      <button
        @click="handlePluralKit"
        class="block p-0.5 rounded-lg bg-gradient-to-br from-sky-400 to-violet-400"
      >
        <div
          class="bg-gradient-to-br from-sky-600 text-center to-violet-600 transition-all hover:brightness-110 block text-gray-100 font-semibold font-display text-lg px-4 py-2 rounded-lg"
        >
          PluralKit
        </div>
      </button>
    </div>
  </OctoCard>
    </div>
  </div>
</template>
<script setup lang="ts">
const token = ref<string | null>(null)

onMounted(() => {
  // Get param from URL
  const urlParams = new URLSearchParams(window.location.search)
  const tokenParam = urlParams.get('token')
  if (tokenParam) {
    token.value = tokenParam
  }
})

const handleFull = () => handleExport('full')
const handlePluralKit = () => handleExport('pk')

const handleExport = async (type: string) => {
  const result = await fetch(`https://api.octocon.app/api/settings/export?type=${type}`, {
    method: 'GET',
    headers: {
      Authorization: `Bearer ${token.value}`
    }
  })

  if (result.status == 429) {
    alert('You can only perform one export every 4 hours.')
    return
  }
  if (!result.ok) {
    alert('An error occurred while performing the export.')
    return
  }

  // Download the exported JSON as a file
  const blob = await result.blob()
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `octocon_export_${type}.json`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
}
</script>
<style scoped>
.octo-gradient {
  background: radial-gradient(
    circle,
    theme('colors.sky.500 / 20%') 0%,
    theme('colors.violet.500 / 10%') 40%,
    theme('colors.violet.500 / 0%') 80%
  );
}
</style>