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
      class="container flex flex-col z-99 items-center px-8 py-24 sm:py-32 md:py-40"
    >
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
        class="font-bold font-display text-4xl md:text-5xl lg:text-6xl 2xl:text-7xl pt-4 pb-8 text-gray-200"
      >
        Octocon
      </h1>
      <IndexHeroCard />
    </div>
    <div class="pb-24 flex flex-col items-center gap-8">
      <h2
        class="font-bold font-display text-xl md:text-2xl lg:text-3xl text-gray-300 text-center"
      >
        Or download the app
      </h2>
      <div class="flex flex-col sm:flex-row justify-center gap-4">
        <ButtonPlayStore />
        <!-- <ButtonAppStore /> -->
      </div>
    </div>
  </div>
</template>
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
