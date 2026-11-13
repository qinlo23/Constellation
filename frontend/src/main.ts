import { createApp } from 'vue'
import App from './App.vue'
import { setupAppPlugins } from '@/core/plugins'
import { initializeUiTheme } from '@/shared/theme'

initializeUiTheme()
const app = createApp(App)

setupAppPlugins(app)
app.mount('#app')
