<template>
  <header
    class="fixed w-screen top-0 left-0 px-4 py-4 flex justify-center z-999"
  >
    <nav
      class="border-[1px] rounded-full border-gray-500 overflow-hidden backdrop-blur-sm bg-gray-950/60"
    >
      <ul class="flex">
        <li v-for="item in navigation" :key="item.name">
          <NuxtLink
            :to="item.href"
            class="block py-1 font-semibold font-display text-md text-gray-300 hover:text-sky-500 transition duration-300 ease-in-out"
            :class="item.class"
          >
            {{ item.name }}
          </NuxtLink>
        </li>
      </ul>
    </nav>
  </header>
</template>

<script setup lang="ts">
const navigation = [
  {
    name: 'Home',
    href: '/',
    class: 'pl-6 pr-3'
  },
  {
    name: 'Docs',
    href: '/docs',
    class: 'pl-3 pr-3'
  },
  {
    name: 'Discord',
    href: 'https://octocon.app/discord',
    class: 'pl-3 pr-6'
  }
]
</script>
