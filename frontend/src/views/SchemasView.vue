<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useSchemaStore } from '@/stores/schema';
import { RouterLink, useRouter } from 'vue-router'; // useRouter importieren
import type { Ref } from 'vue';
import { formatDateDE } from '@/utils/formatters';
import axios from 'axios'; // Axios importieren für API-Aufrufe

const schemaStore = useSchemaStore();
const router = useRouter(); // Router Instanz holen

// Lokaler State, um die Eingabewerte für jedes Schema zu speichern
const localExpectedEntries: Ref<Record<string, number | null>> = ref({});
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api'; // Basis-URL

onMounted(async () => {
  await schemaStore.fetchSchemas();
  // Initialisiere den lokalen State mit den Werten aus dem Store
  schemaStore.schemas.forEach(schema => {
    if (schema.id) {
      localExpectedEntries.value[schema.id] = schema.expectedEntries ?? null;
    }
  });
});

function handleUpdate(schemaId: string) {
  const count = localExpectedEntries.value[schemaId];
  if (count !== null && count >= 0) {
    schemaStore.updateExpectedEntries(schemaId, count);
  }
}

function getFullLink(linkId: string): string {
  // Baut die komplette URL zusammen, die der Planer kopieren kann
  return `${window.location.origin}/submit/${linkId}`;
}

// NEU: Methode zum Navigieren zur Verfügbarkeiten-Detailansicht
function goToAvailabilities(schemaId: string) {
  router.push({ name: 'schema-availabilities', params: { schemaId: schemaId } });
}

// NEU: Methode zum Generieren eines Vorschlags
async function generateVorschlag(schemaId: string) {
  try {
    const response = await axios.post(`${baseURL}/schemas/${schemaId}/generate_proposal`, {}, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });
    // Nimm die ID aus der Antwort
    const newProposalId = response.data.id; // Annahme: Backend gibt das volle Proposal-Objekt zurück
    alert("Dienstplanvorschlag generiert!");
    // Leite zur Detailseite des Vorschlags weiter
    router.push({ name: 'proposal-details', params: { proposalId: newProposalId } });

  } catch (err) {
    console.error("Fehler beim Generieren des Vorschlags:", err);
    alert("Fehler beim Generieren des Vorschlags: " + (err as any).response?.data || (err as any).message);
  }
}

// Methode zum Anzeigen des letzten Vorschlags FÜR EIN SPEZIFISCHES SCHEMA
async function viewLatestProposalForSchema(schemaId: string) {
  try {
    const response = await axios.get<string>(`${baseURL}/proposals/latest/bySchema/${schemaId}/id`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });
    const latestProposalId = response.data;

    if (latestProposalId) {
      router.push({ name: 'proposal-details', params: { proposalId: latestProposalId } });
    } else {
      alert("Für dieses Dienstplanschema wurde noch kein Vorschlag generiert.");
    }
  } catch (err) {
    console.error("Fehler beim Abrufen des letzten Vorschlags für Schema " + schemaId + ":", err);
    alert("Fehler beim Abrufen des letzten Vorschlags: " + (err as any).response?.data?.message || (err as any).message);
  }
}
</script>

<template>
  <div>
    <h1>Deine Dienstpläne</h1>
    <div class="header-actions">
      <RouterLink to="/schemas/new" class="btn-primary">Neuen Dienstplan erstellen</RouterLink>
    </div>

    <div v-if="schemaStore.isLoading" class="loading-indicator">Lade Dienstpläne...</div>

    <div v-else-if="schemaStore.schemas.length > 0" class="schema-list">
      <div v-for="schema in schemaStore.schemas" :key="schema.id" class="schema-card">
        <RouterLink :to="`/schemas/${schema.id}`" class="schema-name-link">
          <h2>{{ schema.name }}</h2>
        </RouterLink>

        <p>{{ formatDateDE(schema.startDate) }} bis {{ formatDateDE(schema.endDate) }}</p>

        <div class="entries-info">
          <p>
            <strong>Antworten:</strong>
            <span class="count">{{ schema.submittedEntriesCount }} / {{ schema.expectedEntries ?? '?' }}</span>
          </p>
          <div class="entries-input">
            <input
              type="number"
              min="0"
              v-model="localExpectedEntries[schema.id!]"
              placeholder="Erwartet"
              @blur="handleUpdate(schema.id!);"
              @keyup.enter="handleUpdate(schema.id!)"
            />
            <button @click="handleUpdate(schema.id!)">Speichern</button>
          </div>
        </div>

        <div v-if="schema.availabilityLinkID" class="link-section">
          <label>Link für Mitarbeiter:</label>
          <input type="text" :value="getFullLink(schema.availabilityLinkID)" readonly />
        </div>

        <div class="card-buttons">
          <button @click="goToAvailabilities(schema.id!)" class="btn-secondary">Verfügbarkeiten</button>
          <button @click="generateVorschlag(schema.id!)" class="btn-primary">Vorschlag generieren</button>
          <button @click="viewLatestProposalForSchema(schema.id!)" class="btn-secondary">Letzten Plan anschauen</button>
        </div>
      </div>
    </div>

    <p v-else class="no-data-message">Du hast noch keine Dienstpläne erstellt.</p>
  </div>
</template>

<style scoped>
/* Bestehende Styles */
.header-actions {
  margin-bottom: 2rem;
}

.btn-primary {
  padding: 0.75rem 1.5rem;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  text-decoration: none;
  border-radius: 8px;
  font-weight: bold;
  border: none; /* Button-Stil */
  cursor: pointer;
}

.btn-primary:hover {
  background-color: hsla(160, 100%, 30%, 1);
}

.btn-secondary { /* Dieser Stil wird jetzt auch für den neuen Button in den Card-Buttons verwendet */
  background-color: #e9ecef;
  color: #333;
  border: 1px solid #ced4da;
}

.btn-secondary:hover {
  background-color: #dae0e5;
}


.schema-list {
  display: grid;
  gap: 1.5rem;
}

.schema-card {
  padding: 1.5rem;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
  display: flex; /* Für die Anordnung der Elemente */
  flex-direction: column;
  gap: 1rem; /* Abstand zwischen den Elementen in der Karte */
}

/* Anpassung für den Schema-Namen als Link */
.schema-name-link {
  text-decoration: none;
  color: #2c3e50;
  display: block; /* Nimmt die volle Breite ein */
}
.schema-name-link h2 {
  font-size: 1.5rem;
  margin: 0 0 0.5rem 0; /* Abstand nach unten */
  color: var(--color-heading); /* Vue Standard Header Farbe */
}
.schema-name-link:hover h2 {
  color: hsla(160, 100%, 37%, 1); /* Hover-Effekt */
}

.link-section {
  margin-top: 0.5rem; /* Weniger Abstand, da Gap gesetzt ist */
}

.link-section label {
  display: block;
  font-weight: bold;
  margin-bottom: 0.25rem; /* Etwas weniger Abstand */
  font-size: 0.9em;
  color: #555;
}

.link-section input {
  width: 100%;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
  background-color: #f9f9f9;
  font-size: 0.9em;
}

.entries-info {
  margin-top: 0.5rem; /* Weniger Abstand, da Gap gesetzt ist */
  padding-top: 0.5rem;
  border-top: 1px solid #eee;
}

.count {
  font-weight: bold;
  font-size: 1.1rem;
  background-color: #f0f0f0;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  margin-left: 0.5rem;
}

.entries-input {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.5rem;
  align-items: center;
}

.entries-input input {
  width: 100px;
  padding: 0.5rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}

.entries-input button {
  padding: 0.5rem 1rem;
  border: 1px solid #ccc;
  background-color: #f0f0f0;
  border-radius: 4px;
  cursor: pointer;
}

/* NEU: Styles für die Buttons am Ende der Karte */
.card-buttons {
  display: flex;
  gap: 0.75rem;
  margin-top: 1.5rem;
}

.card-buttons button {
  flex: 1; /* Buttons nehmen gleiche Breite ein */
  padding: 0.75rem 1rem;
  border-radius: 8px;
  font-weight: bold;
  cursor: pointer;
  border: none;
}

.btn-secondary {
  background-color: #e9ecef; /* Helle Farbe für den Verfügbarkeiten-Button */
  color: #333;
  border: 1px solid #ced4da;
}

.btn-secondary:hover {
  background-color: #dae0e5;
}

.loading-indicator, .no-data-message {
  text-align: center;
  margin-top: 2rem;
  font-size: 1.1em;
  color: #555;
}
</style>
