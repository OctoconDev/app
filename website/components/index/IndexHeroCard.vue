<template>
  <OctoCard>
    <p class="lg:text-lg text-gray-300 text-center">
      Octocon is the modern, all-in-one toolkit for people with DID and OSDD to
      manage their disorder and express themselves.
    </p>
    <div class="flex flex-col-reverse lg:flex-row justify-center gap-4">
      <NuxtLink
        to="/app/"
        class="block p-0.5 rounded-lg bg-gradient-to-br from-sky-400 to-violet-400"
      >
        <div
          class="bg-gradient-to-br from-sky-600 text-center to-violet-600 transition-all hover:brightness-110 block text-gray-100 font-semibold font-display text-lg px-4 py-2 rounded-lg"
        >
          Launch the web app
        </div>
      </NuxtLink>
      <NuxtLink
        to="/docs"
        class="block p-0.5 rounded-lg bg-gradient-to-br from-gray-800 to-gray-900"
      >
        <div
          class="border-sky-600 bg-gradient-to-br from-gray-900 text-center to-gray-800 transition-all hover:brightness-110 flex items-center justify-center gap-2 text-gray-200 font-medium font-display text-lg px-4 py-2 rounded-lg"
        >
          Learn more
        </div>
      </NuxtLink>
    </div>
  </OctoCard>
</template>
<script setup lang="ts"></script>
