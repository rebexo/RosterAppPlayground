<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { formatDateDE } from '@/utils/formatters'; // Annahme, dass du diesen Formatter hast

// Typdefinitionen (könnten in einer separaten types.ts Datei liegen)
interface SchemaDto {
  id: string;
  name: string;
  startDate: string;
  endDate: string;
  expectedEntries: number | null;
  submittedEntriesCount: number;
}

interface AvailabilityEntryDetailDTO {
  id: string;
  staffName: string;
  targetShiftCount: number | null;
  unavailableDates: string[]; // Datum als String im ISO-Format
}

// Router Instanzen
const route = useRoute();
const router = useRouter();

// Props für die Komponente
const schemaId = route.params.schemaId as string; // schemaId kommt über die Route

// Reactive State
const schema = ref<SchemaDto | null>(null);
const availabilityEntries = ref<AvailabilityEntryDetailDTO[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

// Basis-URL für deine API
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

// Funktionen ${schemaId}
const fetchSchemaDetails = async () => {
  try {
    const response = await axios.get<SchemaDto>(`${baseURL}/schemas/${schemaId}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });
    schema.value = response.data;
  } catch (err) {
    console.error("Fehler beim Laden der Schema-Details:", err);
    error.value = "Konnte Schema-Details nicht laden.";
  }
};

const fetchAvailabilityEntries = async () => {
  try {
    const response = await axios.get<AvailabilityEntryDetailDTO[]>(`${baseURL}/schemas/${schemaId}/availability-entries`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });
    // Füge eine lokale Eigenschaft für die Input-Bindung hinzu
    availabilityEntries.value = response.data.map(entry => ({
      ...entry,
      targetShiftCountInput: entry.targetShiftCount // Temporäre Property für v-model
    }));
  } catch (err) {
    console.error("Fehler beim Laden der Verfügbarkeiten:", err);
    error.value = "Konnte Verfügbarkeitseinträge nicht laden.";
  }
};

const setTargetShiftCount = async (entryId: string, count: number | null) => {
  if (count === null || count < 0) {
    alert("Bitte geben Sie eine gültige positive Zahl ein.");
    return;
  }
  try {
    await axios.put(`${baseURL}/availability-entries/${entryId}/target-shift-count`, count, {
      headers: {
        Authorization: `Bearer ${localStorage.getItem('authToken')}`,
        'Content-Type': 'application/json' // Wichtig, da nur ein Integer gesendet wird
      }
    });
    alert("Target Shift Count aktualisiert!");
    // Optional: Verfügbarkeiten neu laden oder nur den Wert lokal aktualisieren
    // await fetchAvailabilityEntries();
  } catch (err) {
    console.error("Fehler beim Aktualisieren des Target Shift Counts:", err);
    error.value = "Fehler beim Aktualisieren des Target Shift Counts.";
    alert("Fehler beim Aktualisieren: " + (err as any).response?.data || (err as any).message);
  }
};

const generateVorschlag = async () => {
  try {
    await axios.post(`${baseURL}/schemas/${schemaId}/generate_proposal`, {}, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });
    alert("Dienstplanvorschlag generiert!");
    // Ggf. Weiterleitung zu einer Vorschlagsansicht
  } catch (err) {
    console.error("Fehler beim Generieren des Vorschlags -console:", err);
    error.value = "Fehler beim Generieren des Vorschlags -error.";
    alert("Fehler beim Generieren des Vorschlags -alert: " + (err as any).response?.data || (err as any).message);
  }
};

const formatUnavailableDates = (dates: string[]): string => {
  if (!dates || dates.length === 0) return '-';
  return dates.map(dateString => formatDateDE(dateString)).join(', ');
};

// Lifecycle Hook
// onMounted(async () => {
//   isLoading.value = true;
//   await Promise.all([
//     fetchSchemaDetails(),
//     fetchAvailabilityEntries()
//   ]);
//   isLoading.value = false;
// });

// Lifecycle Hook
onMounted(async () => {
  isLoading.value = true;
  error.value = null; // Fehler zurücksetzen
  try {
    await Promise.all([
      fetchSchemaDetails(),
      fetchAvailabilityEntries()
    ]);
  } catch (err) {
    // Fehler werden bereits in den einzelnen Fetch-Methoden gesetzt
  } finally {
    isLoading.value = false;
  }
});
</script>

<template>
  <div class="schema-availabilities">
    <button @click="router.push({ name: 'schema-list' })" class="btn-back">
      &larr; zurück zur Dienstplanübersicht
    </button>

    <template v-if="isLoading">
      <div class="loading-indicator">Lade Schema-Details...</div>
    </template>
    <template v-else-if="error">
      <div class="error-message">Fehler beim Laden der Schema-Details.</div>
    </template>
    <template v-else-if="schema">
      <h1>Dienstplan: {{ schema.name }}</h1>
      <p>Erwartete Einträge: {{ schema.expectedEntries ?? '?' }}</p>
      <p>Erfolgte Einträge: {{ schema.submittedEntriesCount }}</p>
    </template>
    <template v-else>
      <h1>Dienstplan: Unbekannt</h1>
      <p>Erwartete Einträge: ?</p>
      <p>Erfolgte Einträge: ?</p>
    </template>


    <div v-if="isLoading && !error" class="loading-indicator">Lade Verfügbarkeiten...</div>
    <div v-else-if="error" class="error-message">{{ error }}</div>

    <div v-else class="availability-table-container">
      <div v-if="availabilityEntries.length === 0" class="no-entries-message">
        Für diesen Dienstplan wurden noch keine Verfügbarkeiten eingereicht.
      </div>
      <table v-else>
        <thead>
        <tr>
          <th>Mitarbeitername</th>
          <th>Nicht verfügbar</th>
          <th>Zielschichten</th>
          <th>Ändern</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="entry in availabilityEntries" :key="entry.id">
          <td>{{ entry.staffName }}</td>
          <td>{{ formatUnavailableDates(entry.unavailableDates) }}</td>
          <td>
            <input type="number" min="0" v-model.number="entry.targetShiftCount" />
          </td>
          <td>
            <button @click="setTargetShiftCount(entry.id, entry.targetShiftCount)">Ziel setzen</button>
          </td>
        </tr>
        </tbody>
      </table>
    </div>

    <button @click="generateVorschlag()" class="btn-primary generate-button">Vorschlag generieren</button>
  </div>
</template>

<style scoped>
.schema-availabilities {
  padding: 2rem;
  max-width: 1000px;
  margin: 0 auto;
}

.btn-back {
  background: #f0f0f0;
  border: 1px solid #ccc;
  padding: 0.5rem 1rem;
  border-radius: 5px;
  cursor: pointer;
  margin-bottom: 1.5rem;
}

.availability-table-container {
  margin-top: 1.5rem;
  overflow-x: auto; /* Für responsive Tabellen */
}

table {
  width: 100%;
  border-collapse: collapse;
  background-color: white;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  border-radius: 8px;
  overflow: hidden; /* Für abgerundete Ecken der Tabelle */
}

th, td {
  border: 1px solid #e0e0e0;
  padding: 12px 15px;
  text-align: left;
}

th {
  background-color: #f8f8f8;
  font-weight: bold;
  color: #333;
  text-transform: uppercase;
  font-size: 0.9em;
}

tr:nth-child(even) {
  background-color: #f9f9f9;
}

input[type="number"] {
  width: 80px;
  padding: 8px;
  border: 1px solid #ccc;
  border-radius: 4px;
}

td button {
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  border: none;
  padding: 8px 12px;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

td button:hover {
  background-color: hsla(160, 100%, 30%, 1);
}

.generate-button {
  margin-top: 2rem;
  background-color: #007bff; /* Beispiel: Blaue Farbe für den Button */
}

.loading-indicator, .error-message {
  text-align: center;
  margin-top: 2rem;
  font-size: 1.1em;
  color: #555;
}
.error-message {
  color: #dc3545; /* Rot für Fehlermeldungen */
}
</style>
