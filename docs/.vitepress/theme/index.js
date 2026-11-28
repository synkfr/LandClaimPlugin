import DefaultTheme from 'vitepress/theme'
import './tokens.css'
import './base.css'
import './custom.css'
import LcpHome from './components/LcpHome.vue'

export default {
  extends: DefaultTheme,
  enhanceApp({ app }) {
    app.component('LcpHome', LcpHome)
  }
}
