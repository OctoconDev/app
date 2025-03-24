<template>
  <div
    class="bg-gray-950/50 border border-gray-500 px-4 py-4 rounded-xl flex flex-col gap-2"
  >
    <p class="font-display font-semibold text-lg">{{ data.title }}</p>
    <NuxtLink
      v-for="item in data.children"
      :key="item._path"
      :to="item._path"
      class="rounded-md flex items-center gap-2 p-2 bg-gray-900 hover:bg-gray-800 text-gray-300 hover:text-gray-200 font-medium transition duration-300 ease-in-out"
    >
      <UnoIcon v-if="item.icon" :class="item.icon" class="block text-xl" />
      {{ item.title }}
    </NuxtLink>
  </div>
</template>

<script setup lang="ts">
import type { NavItem } from '@nuxt/content/dist/runtime/types'

defineProps<{
  data: NavItem
}>()
</script>

<style scoped>
.router-link-exact-active {
  @apply bg-gradient-to-br from-sky-600 to-violet-600 font-bold text-gray-100;
}
</style>
