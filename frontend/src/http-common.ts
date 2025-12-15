import axios from "axios";

// Hier holt sich Vite die Variable aus der .env Datei
// Falls keine da ist -> localhost als Fallback
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  headers: {
    "Content-type": "application/json",
  },
});

apiClient.interceptors.request.use(
  (config) => {
    // 1. Wir holen den Token direkt unter dem Namen, den du benutzt hast
    const token = localStorage.getItem('authToken');

    // 2. Wenn ein Token da ist, kleben wir ihn an den Header
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default apiClient;
