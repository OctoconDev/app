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
    <div class="container flex flex-col z-99 items-center px-4 py-12">
      <div class="relative h-[128px] w-[128px]">
        <NuxtPicture
          src="/img/octocon-logo.png"
          alt="Octocon logo"
          format="avif,webp"
          width="128"
          height="128"
          sizes="256"
          quality="90"
        />
        <div
          class="rounded-full octo-gradient absolute inset--100% md:inset--200% z--1 blur-lg"
        />
      </div>
      <h1
        class="font-bold font-display text-3xl md:text-4xl lg:text-5xl 2xl:text-6xl pt-4 pb-8 text-gray-200"
      >
        Whoops!
      </h1>
      <OctoCard>
        <p class="lg:text-lg text-gray-300 text-center">
          It looks like you're trying to access a link that leads to the Octocon
          app. Your browser isn't detecting this properly, so you're seeing this
          page instead.
        </p>
      </OctoCard>
    </div>
    <div class="flex flex-col items-center gap-8 px-4 pb-12">
      <h2
        class="font-bold font-display text-2xl md:text-3xl lg:text-4xl text-gray-300 text-center"
      >
        Firefox users:
      </h2>
      <OctoCard>
        <p class="lg:text-lg text-gray-300 text-center">
          Click the three dots in the top right corner of your browser, then
          navigate to <b>Settings</b> -> <b>Advanced</b> ->
          <b>Open links in apps</b> and set it to <b>Always</b>. Then, reload
          this page.
        </p>
        <div class="w-full grid place-items-center">
          <NuxtPicture
            src="/img/enable-firefox.png"
            alt="Enable Firefox setting"
            format="avif,webp"
            sizes="256"
            quality="90"
          />
        </div>
      </OctoCard>
    </div>
    <div class="flex flex-col items-center gap-8 px-4 pb-12">
      <h2
        class="font-bold font-display text-2xl md:text-3xl lg:text-4xl text-gray-300 text-center"
      >
        Samsung Internet users:
      </h2>
      <OctoCard>
        <p class="lg:text-lg text-gray-300 text-center">
          Click the small Octocon icon in the top right of your screen by the
          address bar. If you do not see it, try using a different browser.
        </p>
        <div class="w-full grid place-items-center">
          <NuxtPicture
            src="/img/enable-samsung-internet.png"
            alt="Enable Samsung Internet setting"
            format="avif,webp"
            sizes="256"
            quality="90"
          />
        </div>
      </OctoCard>
    </div>
    <div class="flex flex-col items-center gap-8 px-4 pb-12">
      <h2
        class="font-bold font-display text-2xl md:text-3xl lg:text-4xl text-gray-300 text-center"
      >
        Other browsers:
      </h2>
      <OctoCard>
        <p class="lg:text-lg text-gray-300 text-center">
          Please try using a different browser. We recommend Firefox, but most
          devices come preinstalled with Google Chrome, which will work by
          default.
        </p>
      </OctoCard>
    </div>
  </div>
</template>
<script setup lang="ts">
useHead({
  title: 'Link access'
})

definePageMeta({
  layout: 'bare'
})

// const event = useRequestEvent()
// if (event) {
//  setResponseStatus(event, 404)
// }
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
