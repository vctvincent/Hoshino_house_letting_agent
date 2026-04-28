import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './styles/variables.css'
import './styles/global-amounts.css'
import './styles/dashboard-styles.css'
import './styles/page-shell.css'
import App from './App.vue'
import router from './router'
import store from './store'

const app = createApp(App)

app.use(ElementPlus)
app.use(router)
app.use(store)

app.mount('#app')
