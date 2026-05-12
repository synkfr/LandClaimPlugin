import { defineConfig } from 'vitepress'

export default defineConfig({
  title: "LandClaimPlugin",
  description: "Advanced Territory Protection for Minecraft",
  base: "/LandClaimPlugin/",

  head: [
    ['meta', { name: 'theme-color', content: '#e6a817' }],
    ['meta', { property: 'og:title', content: 'LandClaimPlugin' }],
    ['meta', { property: 'og:description', content: 'Advanced Territory Protection for Minecraft' }],
  ],

  themeConfig: {
    logo: 'https://i.postimg.cc/jS6mh13k/minecraft-title-2.png',
    siteTitle: false,

    nav: [
      { text: 'Guide', link: '/guide/installation' },
      { text: 'Commands', link: '/guide/commands' },
      { text: 'Config', link: '/CONFIGURATION' },
      { text: 'API', link: '/guide/api' },
      { text: 'FAQ', link: '/FAQ' }
    ],

    sidebar: {
      '/guide/': [
        {
          text: 'User Guide',
          items: [
            { text: 'Installation', link: '/guide/installation' },
            { text: 'Commands & Permissions', link: '/guide/commands' },
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
            { text: 'Database', link: '/DATABASE' },
            { text: 'Developer Guide', link: '/DEVELOPMENT' }
          ]
        }
      ],
      '/': [
        {
          text: 'User Guide',
          items: [
            { text: 'Installation', link: '/guide/installation' },
            { text: 'Commands & Permissions', link: '/guide/commands' },
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
            { text: 'Database', link: '/DATABASE' },
            { text: 'Developer Guide', link: '/DEVELOPMENT' }
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/synkfr/LandClaimPlugin' },
      { icon: 'discord', link: 'https://discord.gg/fGyDyp3Ak4' }
    ],

    search: {
      provider: 'local'
    },

    editLink: {
      pattern: 'https://github.com/synkfr/LandClaimPlugin/edit/v2/docs/:path',
      text: 'Edit this page on GitHub'
    },

    footer: {
      message: 'Released under the MIT License.',
      copyright: 'Copyright © 2024-present AyoSynk'
    }
  }
})
