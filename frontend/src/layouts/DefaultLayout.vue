<script setup lang="ts">
import { RouterLink, RouterView, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

function handleLogout() {
  authStore.logout()
  router.push({ name: 'login' })
}
</script>

<template>
  <header v-if="authStore.isAuthenticated">
    <nav class="navbar">
      <div class="nav-links">
        <RouterLink to="/">Dashboard</RouterLink>
        <RouterLink to="/templates">Templates</RouterLink>
        <RouterLink to="/schemas">Dienstpläne</RouterLink>
        <RouterLink to="/shifts">Schichten</RouterLink>
      </div>
      <div class="nav-actions">
        <span class="username">Eingeloggt als: {{ authStore.user?.username }}</span>
        <button @click="handleLogout" class="logout-button">Logout</button>
      </div>
    </nav>
  </header>
  <main>
    <RouterView />
  </main>
</template>


<style scoped>
header {
  /* Stellt sicher, dass der Header Platz einnimmt, damit der main-Inhalt nicht darunter rutscht */
  height: 60px;
  width: 100%;
}

.navbar {
  position: fixed; /* Heftet die Leiste oben an */
  top: 0;
  left: 0;
  width: 100%;
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 2rem;
  z-index: 1000;
}

.nav-links a {
  font-weight: 500;
  color: #2c3e50;
  text-decoration: none;
  margin-right: 1.5rem;
  padding: 0.5rem 0;
}

/* Visuelles Feedback für die aktive Seite */
.nav-links a.router-link-exact-active {
  color: hsla(160, 100%, 37%, 1);
  border-bottom: 2px solid hsla(160, 100%, 37%, 1);
}

.nav-actions {
  display: flex;
  align-items: center;
}

.username {
  margin-right: 1rem;
  font-size: 0.9rem;
  color: #555;
}

.logout-button {
  background-color: transparent;
  border: 1px solid #ccc;
  padding: 0.4rem 0.8rem;
  border-radius: 4px;
  cursor: pointer;
}

/* Hauptinhalt bekommt Abstand nach oben, damit er nicht von der Leiste verdeckt wird */
main {
  padding: 2rem;
}
</style>
