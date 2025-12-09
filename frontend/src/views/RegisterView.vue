<script setup lang="ts">
import { ref } from 'vue';
import { useAuthStore } from '@/stores/auth';
import { useRouter, RouterLink } from 'vue-router';

const authStore = useAuthStore();
const router = useRouter();

const username = ref('');
const password = ref('');
const confirmPassword = ref('');
const error = ref('');

async function handleRegister() {
  error.value = '';
  try {
    await authStore.register({
      username: username.value,
      password: password.value,
      confirmPassword: confirmPassword.value
    });
    // Bei Erfolg direkt zum Dashboard weiterleiten
    router.push({ name: 'dashboard' });
  } catch (err: any) {
    console.error("Fehler bei der Registrierung:", err);
    error.value = err.message || 'Registrierung fehlgeschlagen.';
  }
}
</script>

<template>
  <div class="form-container">
    <h2>Account erstellen</h2>
    <form @submit.prevent="handleRegister">
      <input v-model="username" type="text" placeholder="Benutzername" required />
      <input v-model="password" type="password" placeholder="Passwort" required />
      <input v-model="confirmPassword" type="password" placeholder="Passwort bestätigen" required />
      <button type="submit">Registrieren</button>
    </form>
    <p v-if="error" class="error-message">{{ error }}</p>

    <div class="redirect-link">
      <span>Bereits einen Account?</span>
      <RouterLink to="/login">Jetzt anmelden</RouterLink>
    </div>
  </div>
</template>

<style scoped>
/* Du kannst die gleichen Styles wie in der LoginView verwenden */
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
