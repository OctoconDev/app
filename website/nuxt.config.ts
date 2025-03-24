export default defineNuxtConfig({
  devtools: {
    enabled: true,
    timeline: {
      enabled: true
    }
  },

  modules: [
    '@nuxtjs/eslint-module',
    '@nuxt/image',
    '@unocss/nuxt',
    '@vueuse/nuxt',
    '@nuxt/content',
    '@nuxtjs/seo'
  ],

  image: {},

  content: {
    highlight: false
  },

  app: {
    head: {
      link: [
        {
          rel: 'apple-touch-icon',
          sizes: '180x180',
          href: '/apple-touch-icon.png'
        },
        {
          rel: 'icon',
          type: 'image/png',
          sizes: '16x16',
          href: '/favicon-16x16.png'
        },
        {
          rel: 'icon',
          type: 'image/png',
          sizes: '32x32',
          href: '/favicon-32x32.png'
        },
        {
          rel: 'manifest',
          href: '/site.webmanifest'
        }
      ],
      script: [
        {
          // <!-- Cloudflare Web Analytics --><script defer src='https://static.cloudflareinsights.com/beacon.min.js' data-cf-beacon='{"token": "02047a1b340248b3937f03af8f0efdbf"}'></script><!-- End Cloudflare Web Analytics -->
          defer: true,
          src: 'https://static.cloudflareinsights.com/beacon.min.js',
          'data-cf-beacon': '{"token": "02047a1b340248b3937f03af8f0efdbf"}',
          tagPosition: 'bodyClose'
        }
      ]
    }
  },

  routeRules: {
    '/docs': {
      redirect: '/docs/introduction'
    },
    '/docs/app': {
      redirect: '/docs/app/download'
    },
    '/docs/discord': {
      redirect: '/docs/discord/setup'
    },
    '/docs/community': {
      redirect: '/docs/community/getting-started'
    },
    '/docs/contributing': {
      redirect: '/docs/contributing/getting-started'
    },
    '/docs/**': { isr: true }
  },

  nitro: {
    prerender: {
      failOnError: false,
      routes: [
        '/',
        '/sitemap.xml',
        '/robots.txt',
        '/_ipx/f_webp&q_90&s_512x512/img/octocon-logo.png',
        '/_ipx/f_avif&q_90&s_256x256/img/octocon-logo.png',
        '/_ipx/f_webp&q_90&s_256x256/img/octocon-logo.png',
        '/_ipx/f_png&q_90&s_256x256/img/octocon-logo.png',
        '/_ipx/f_png&q_90&s_512x512/img/octocon-logo.png',
        '/_ipx/f_avif&q_90&s_512x512/img/octocon-logo.png',

        '/_ipx/w_256&f_webp&q_90/img/enable-samsung-internet.png',
        '/_ipx/w_512&f_webp&q_90/img/enable-samsung-internet.png',
        '/_ipx/w_256&f_avif&q_90/img/enable-samsung-internet.png',
        '/_ipx/w_512&f_avif&q_90/img/enable-samsung-internet.png',

        '/_ipx/w_256&f_webp&q_90/img/enable-firefox.png',
        '/_ipx/w_512&f_webp&q_90/img/enable-firefox.png',
        '/_ipx/w_256&f_avif&q_90/img/enable-firefox.png',
        '/_ipx/w_512&f_avif&q_90/img/enable-firefox.png'
      ]
    }
  },

  future: {
    typescriptBundlerResolution: true
  },

  site: {
    url: 'https://octocon.app',
    name: 'Octocon',
    description:
      'The modern, all-in-one toolkit for people with DID and OSDD to manage their disorder and express themselves.',
    defaultLocale: 'en'
  },

  sitemap: {
    strictNuxtContentPaths: true
  },

  compatibilityDate: '2025-02-11'
})