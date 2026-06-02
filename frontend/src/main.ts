import { createPinia } from 'pinia'
import { createApp } from 'vue'

import App from './app.vue'
import naive from 'naive-ui'
import router from './router'
import pinia from './store'
import permission from './directives/permission'
import './assets/scrollbar.css'

const app = createApp(App)

app.use(pinia)
app.use(router)
app.use(naive)
app.directive('permission', permission)

app.mount('#app')
