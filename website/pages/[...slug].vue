<template>
  <div
    id="content"
    class="px-8 bg-gradient-to-b from-gray-950 to-gray-900 min-h-screen text-white flex justify-center"
  >
    <div class="flex container gap-8 justify-center">
      <button
        class="z-999 fixed left-4 bottom-4 block lg:hidden p-4 rounded-xl bg-gray-950/50 backdrop-blur-sm"
        @click="isNavOpen = !isNavOpen"
      >
        <UnoIcon
          class="text-4xl text-gray-400"
          :class="
            isNavOpen
              ? 'i-material-symbols-close-rounded'
              : 'i-material-symbols-menu-rounded'
          "
        />
      </button>
      <aside
        :class="
          isNavOpen
            ? 'opacity-100'
            : 'opacity-0 lg:opacity-100 pointer-events-none lg:pointer-events-auto'
        "
        class="fixed inset-0 overflow-y-scroll backdrop-blur-md bg-gray-950/75 grid place-items-center lg:block lg:position-unset lg:backdrop-blur-0 lg:bg-transparent transition duration-300 ease-in-out"
      >
        <nav class="flex w-[16rem] flex-col gap-4 py-24 sm:py-32 md:py-32">
          <template v-for="child in navigationData.children" :key="child._path">
            <DocsNavGroup v-if="child.children" :data="child" />
            <DocsNavItem v-else :data="child" />
          </template>
        </nav>
      </aside>
      <ContentDoc
        class="font-body prose prose-invert prose-gray max-w-auto! w-full lg:w-80ch! py-24 sm:py-32 md:py-32"
      >
        <template #not-found>
          <div
            class="font-body prose prose-invert prose-gray w-80ch! py-24 sm:py-32 md:py-32"
          >
            <h1>404</h1>
            <p>
              Page not found! Check the URL or
              <NuxtLink to="/">go home</NuxtLink>.
            </p>
          </div>
        </template>
      </ContentDoc>
    </div>
  </div>
</template>
<script setup lang="ts">
import type { NavItem } from '@nuxt/content/dist/runtime/types'

const isNavOpen = ref(false)

const router = useRouter()
const removeHook = router.afterEach(() => (isNavOpen.value = false))
onUnmounted(removeHook)

const { data: _navigationData } = await useAsyncData('navigation', () =>
  fetchContentNavigation()
)

const navigationData: NavItem = computed(() => _navigationData.value[0])

useHead({
  htmlAttrs: {
    class: computed(() =>
      isNavOpen.value ? 'overflow-hidden lg:overflow-auto' : ''
    )
  }
})
</script>
<style>
.prose h1 {
  @apply font-display text-sky-400;
}

.prose :where(h2, h3, h4, h5, h6) a {
  @apply text-violet-400! decoration-none font-display;
}

.prose a:not(.reset) {
  @apply text-sky-400;
}

.prose code {
  @apply inline-block px-2 bg-violet-950 rounded-lg text-violet-200;
}

.prose code::before,
.prose code::after {
  content: none;
}
</style>
