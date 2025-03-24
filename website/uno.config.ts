import {
  defineConfig,
  presetIcons,
  presetTypography,
  presetUno,
  presetWebFonts,
  transformerDirectives,
  transformerVariantGroup
} from 'unocss'
import extractorMdc from '@unocss/extractor-mdc'

export default defineConfig({
  presets: [
    presetUno(),
    presetTypography(),
    presetIcons(),
    presetWebFonts({
      provider: 'bunny',
      fonts: {
        display: 'Ubuntu:500,700',
        body: 'Inter:400,500,600'
      }
    })
  ],
  extractors: [extractorMdc()],
  transformers: [transformerDirectives(), transformerVariantGroup()],
  content: {
    filesystem: ['content/**/*.md']
  }
})
