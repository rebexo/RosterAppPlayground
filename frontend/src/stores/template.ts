import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { Ref } from 'vue';
import axios from 'axios';
import { useRouter } from 'vue-router';
import apiClient from '@/http-common.ts'

// Interfaces für die Datenstruktur
export interface TemplateShift {
  weekday: number;
  positionName: string;
  shiftId: string;
}

export interface Template {
  id?: string;
  name: string;
  description?: string;
  shifts: TemplateShift[];
}

export const useTemplateStore = defineStore('template', () => {
  const router = useRouter();

  // State
  const templates: Ref<Template[]> = ref([]);
  const isLoading = ref(false);

  // Actions
  async function fetchTemplates() {
    isLoading.value = true;
    try {
      const response = await apiClient.get('/templates');
      templates.value = response.data;
    } catch (error) {
      console.error('Fehler beim Laden der Templates:', error);
    } finally {
      isLoading.value = false;
    }
  }

  async function createTemplate(templateData: Template) {
    isLoading.value = true;
    try {
      await apiClient.post('/templates', templateData);
      // Nach Erfolg zur Übersichtsseite navigieren
      router.push({ name: 'template-list' });
    } catch (error) {
      console.error('Fehler beim Erstellen des Templates:', error);
      // Hier könntest du einen Fehler im State speichern und anzeigen
    } finally {
      isLoading.value = false;
    }
  }

  async function updateTemplate(templateData: Template) {
    isLoading.value = true;
    try {
      const response = await apiClient.put(`/templates/${templateData.id}`, templateData);
      const index = templates.value.findIndex(t => t.id === templateData.id);
      if (index !== -1) {
        templates.value[index] = response.data;
      }
    } catch (error) { /* ... Error Handling ... */ }
    finally { isLoading.value = false; }
  }

  async function deleteTemplate(templateId: string) {
    isLoading.value = true;
    try {
      await apiClient.delete(`/templates/${templateId}`);
      templates.value = templates.value.filter(t => t.id !== templateId);
    } catch (error) { /* ... Error Handling ... */ }
    finally { isLoading.value = false; }
  }

  return { templates, isLoading, fetchTemplates, createTemplate, updateTemplate, deleteTemplate };
});
