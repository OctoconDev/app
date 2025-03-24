<template>
  <main>
    <slot />
  </main>
</template>
<script setup lang="ts">
useHead({
  bodyAttrs: {
    class: 'font-body bg-gray-950'
  }
})
</script>