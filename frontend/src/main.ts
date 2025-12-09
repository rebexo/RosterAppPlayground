import './assets/main.css'
//import '@fullcalendar/core/vdom'
//import '@fullcalendar/daygrid/main.css'
//import '@fullcalendar/timegrid/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

// Im Entwicklungsmodus den Auth-Token aus dem lokalen Speicher entfernen
if (import.meta.env.DEV) {
  localStorage.removeItem('authToken');
}

const app = createApp(App)

app.use(createPinia())
app.use(router)

app.mount('#app')
