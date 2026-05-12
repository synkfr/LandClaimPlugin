import { defineConfig } from 'vitepress'

export default defineConfig({
  title: "LandClaimPlugin",
  description: "Advanced Territory Protection for Minecraft",
  base: "/LandClaimPlugin/",
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Guide', link: '/CONFIGURATION' },
      { text: 'API', link: '/API' }
    ],

    sidebar: [
      {
        text: 'Getting Started',
        items: [
          { text: 'Home', link: '/' },
          { text: 'FAQ', link: '/FAQ' },
          { text: 'Configuration', link: '/CONFIGURATION' }
        ]
      },
      {
        text: 'Development',
        items: [
          { text: 'Architecture', link: '/ARCHITECTURE' },
          { text: 'Database', link: '/DATABASE' },
          { text: 'Developer Guide', link: '/DEVELOPMENT' },
          { text: 'API Reference', link: '/API' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/synkfr/LandClaimPlugin' },
      { icon: 'discord', link: 'https://discord.gg/pAPPvSmWRK' }
    ],
    
    search: {
      provider: 'local'
    }
  }
})
