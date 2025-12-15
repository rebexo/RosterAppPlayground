import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { Ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import apiClient from "@/http-common.ts";

// Interfaces für die Datenstruktur
export interface SchemaTemplateAssignment {
  templateId: string;
  validFrom: string; // YYYY-MM-DD
  validTo: string;   // YYYY-MM-DD
}

export interface Schema {
  id?: string;
  name: string;
  startDate: string;
  endDate: string;
  availabilityLinkID?: string;
  collectionClosed?: boolean;
  expectedEntries?: number;
  submittedEntriesCount?: number;
  templateAssignments: SchemaTemplateAssignment[];
}

export interface CalculatedShift {
  title: string;
  date: string;
  startTime: string;
  endTime: string;
}

export const useSchemaStore = defineStore('schema', () => {
  const router = useRouter();

  // State
  const schemas: Ref<Schema[]> = ref([]);
  const isLoading = ref(false);
  const currentSchemaDetails: Ref<CalculatedShift[]> = ref([]);

  // Actions
  async function fetchSchemas() {
    isLoading.value = true;
    try {
      const response = await apiClient.get('/schemas');
      schemas.value = response.data;
    } catch (error) {
      console.error('Fehler beim Laden der Schemata:', error);
    } finally {
      isLoading.value = false;
    }
  }

  async function createSchema(schemaData: Schema) {
    isLoading.value = true;
    try {
      await apiClient.post('/schemas', schemaData);
      // Nach Erfolg zur Übersichtsseite navigieren
      router.push({ name: 'schema-list' });
    } catch (error) {
      console.error('Fehler beim Erstellen des Schemas:', error);
      alert('Schema konnte nicht erstellt werden. Prüfe die Eingaben.');
    } finally {
      isLoading.value = false;
    }
  }

  async function fetchSchemaDetails(schemaId: string) {
    isLoading.value = true;
    try {
      const response = await apiClient.get(`/schemas/${schemaId}/details`);
      currentSchemaDetails.value = response.data;
    } catch (error) { /* ... */ }
    finally { isLoading.value = false; }
  }

  async function updateExpectedEntries(schemaId: string, count: number) {
    try {
      const response = await apiClient.patch(`/schemas/${schemaId}/expected-entries`, count, {
        headers: { 'Content-Type': 'application/json' }
      });
      // Update den lokalen State für sofortiges Feedback
      const index = schemas.value.findIndex(s => s.id === schemaId);
      if (index !== -1) {
        schemas.value[index] = response.data;
      }
    } catch (error) {
      console.error('Fehler beim Aktualisieren der erwarteten Einträge:', error);
      alert('Speichern fehlgeschlagen.');
    }
  }

  return { schemas, isLoading, currentSchemaDetails, fetchSchemas, createSchema, fetchSchemaDetails , updateExpectedEntries};
});
