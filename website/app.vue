<template>
  <div>
    <NuxtLayout>
      <NuxtPage />
    </NuxtLayout>
  </div>
</template>

<script setup lang="ts">
import '@unocss/reset/tailwind.css'

useHead({
  titleTemplate: '%s - Octocon'
})
</script>

<style>
html,
body {
  overflow-x: hidden;
}
</style>
