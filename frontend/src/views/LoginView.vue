<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const username = ref('');
const password = ref('');
const error = ref('');

async function handleLogin() {
  try {
    await authStore.login({ username: username.value, password: password.value });
    // Bei Erfolg zum Dashboard weiterleiten
    router.push({ name: 'dashboard' });
  } catch (err) {
    console.error("Fehler beim Login:", err);
    error.value = 'Login fehlgeschlagen. Bitte überprüfe deine Daten.';
  }
}
</script>

<template>
  <div class="form-container">
    <h2>Anmelden</h2>
    <form @submit.prevent="handleLogin">
      <input v-model="username" type="text" placeholder="Benutzername" required />
      <input v-model="password" type="password" placeholder="Passwort" required />
      <button type="submit">Login</button>
    </form>
    <p v-if="error" class="error-message">{{ error }}</p>

    <div class="redirect-link">
      <span>Noch keinen Account?</span>
      <RouterLink to="/register">Jetzt registrieren</RouterLink>
    </div>

  </div>
</template>

<style scoped>
.form-container {
  max-width: 400px;
  margin: 5rem auto;
  padding: 2rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  text-align: center;
}

.error-message {
  color: red;
}

.redirect-link {
  margin-top: 1.5rem;
  font-size: 0.9rem;
}

.redirect-link a {
  margin-left: 0.5rem;
  font-weight: bold;
  color: hsla(160, 100%, 37%, 1);
}
</style>
