import { defineStore } from 'pinia'
import axios from 'axios'
import { jwtDecode } from 'jwt-decode'
import apiClient from "@/http-common.ts";

interface User {
  username: string;
  // roles: string[];
}

interface LoginCredentials {
  username: string;
  password: string;
}

interface RegisterCredentials {
  username: string;
  password: string;
  confirmPassword: string;
}

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('authToken') as string || null,
    user: null as User | null
  }),

  getters: {
    isAuthenticated: (state) => !!state.token
  },

  actions: {
    async login(credentials: LoginCredentials) {
      const response = await apiClient.post('/auth/login', credentials)
      const token = response.data.token
      this.token = token
      localStorage.setItem('authToken', token)
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`

      // Token dekodieren und User-Informationen speichern
      const decodedToken: { sub: string } = jwtDecode(token);
      this.user = { username: decodedToken.sub }; // 'sub' ist der Standard-Claim für den Usernamen im JWT
    },

    async register(credentials: RegisterCredentials) {
      if (credentials.password !== credentials.confirmPassword) {
        throw new Error("Die Passwörter stimmen nicht überein.");
      }

      const response = await apiClient.post('/auth/register', {
        username: credentials.username,
        password: credentials.password,
        confirmPassword: credentials.confirmPassword // Backend-Validierung auch nutzen
      });

      // Nach erfolgreicher Registrierung den User direkt einloggen
      const token = response.data.token;
      this.token = token;
      localStorage.setItem('authToken', token);
      axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;

      const decodedToken: { sub: string } = jwtDecode(token);
      this.user = { username: decodedToken.sub };
    },

    logout() {
      this.token = null
      this.user = null
      localStorage.removeItem('authToken')
      delete axios.defaults.headers.common['Authorization']
    }
  }
})
