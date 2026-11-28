import { defineConfig } from 'vitepress'

export default defineConfig({
  title: 'LandClaimPlugin',
  description: 'Chunk-based territory protection, custom roles, web maps, and per-claim toggles for Paper servers.',
  base: '/LandClaimPlugin/',
  lang: 'en-US',
  appearance: false,

  head: [
    ['meta', { name: 'theme-color', content: '#3b6cf5' }],
    ['meta', { property: 'og:title', content: 'LandClaimPlugin' }],
    ['meta', { property: 'og:description', content: 'Chunk-based territory protection for Paper servers.' }],
    ['link', { rel: 'preconnect', href: 'https://fonts.googleapis.com' }],
    ['link', { rel: 'preconnect', href: 'https://fonts.gstatic.com', crossorigin: '' }],
    ['link', { rel: 'stylesheet', href: 'https://fonts.googleapis.com/css2?family=Familjen+Grotesk:wght@500;600&family=IBM+Plex+Sans:wght@400;500&family=JetBrains+Mono:wght@400;500&display=swap' }]
  ],

  themeConfig: {
    siteTitle: 'LandClaimPlugin',

    nav: [
      { text: 'Guide', link: '/guide/installation' },
      { text: 'Commands', link: '/guide/commands' },
      { text: 'Config', link: '/CONFIGURATION' },
      { text: 'Integrations', link: '/guide/integrations' },
      { text: 'API', link: '/guide/api' },
      { text: 'FAQ', link: '/FAQ' }
    ],

    sidebar: {
      '/': [
        {
          text: 'User guide',
          items: [
            { text: 'Installation', link: '/guide/installation' },
            { text: 'Commands and permissions', link: '/guide/commands' },
            { text: 'Placeholders', link: '/guide/placeholders' },
            { text: 'Integrations', link: '/guide/integrations' },
            { text: 'Configuration', link: '/CONFIGURATION' },
            { text: 'FAQ', link: '/FAQ' }
          ]
        },
        {
          text: 'Developer',
          items: [
            { text: 'Public API', link: '/guide/api' },
            { text: 'Internal API', link: '/API' },
            { text: 'Architecture', link: '/ARCHITECTURE' },
            { text: 'Database schema', link: '/DATABASE' },
            { text: 'Developer guide', link: '/DEVELOPMENT' }
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/synkfr/LandClaimPlugin' },
      { icon: 'discord', link: 'https://discord.gg/UAgsmk6KEr' }
    ],

    search: {
      provider: 'local'
    },

    editLink: {
      pattern: 'https://github.com/synkfr/LandClaimPlugin/edit/master/docs/:path',
      text: 'Edit this page on GitHub'
    },

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'AyoSynk / LandClaimPlugin'
    }
  }
})
