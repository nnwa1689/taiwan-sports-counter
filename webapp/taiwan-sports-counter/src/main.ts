/**
 * main.ts
 *
 * Bootstraps Vuetify and other plugins then mounts the App`
 */

// Composables
import { createApp } from 'vue'

// Plugins
import { registerPlugins } from '@/plugins'

// Components
import App from './App.vue'

// 引入全域字體設定
import './assets/fonts.css'

const app = createApp(App)

registerPlugins(app)

app.mount('#app')
