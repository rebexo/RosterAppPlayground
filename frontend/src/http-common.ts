import axios from "axios";

// Hier holt sich Vite die Variable aus der .env Datei
// Falls keine da ist -> localhost als Fallback
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api",
  headers: {
    "Content-type": "application/json",
  },
});

export default apiClient;
