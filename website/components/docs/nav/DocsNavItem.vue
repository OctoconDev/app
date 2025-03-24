<template>
  <div class="px-4">
    <NuxtLink
      :to="data._path"
      class="rounded-md flex items-center gap-2 p-2 bg-gray-900 hover:bg-gray-800 text-gray-300 hover:text-gray-200 font-medium transition duration-300 ease-in-out"
    >
      <UnoIcon v-if="data.icon" :class="data.icon" class="block text-xl" />
      {{ data.title }}
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
