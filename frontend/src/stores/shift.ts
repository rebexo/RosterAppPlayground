import { defineStore } from 'pinia'
import axios from 'axios'
import type { Ref } from 'vue'
import { ref } from 'vue'

// Eine Schablone für ein Shift-Objekt im Frontend
export interface Shift {
  id: string; // UUID ist ein String
  name: string;
  startTime: string; // z.B. "06:00:00"
  endTime: string;
}

export const useShiftStore = defineStore('shift', () => {
  // State
  const shifts: Ref<Shift[]> = ref([])
  const isLoading = ref(false)

  // Actions
  async function fetchShifts() {
    isLoading.value = true
    try {
      const response = await axios.get('http://localhost:8080/api/shifts')
      shifts.value = response.data
    } catch (error) {
      console.error('Fehler beim Laden der Schichten:', error)
      // Hier könntest du eine Fehlermeldung im State speichern
    } finally {
      isLoading.value = false
    }
  }

  async function createShift(newShift: Omit<Shift, 'id'>) {
    isLoading.value = true;
    try {
      const response = await axios.post('http://localhost:8080/api/shifts', newShift);
      // Füge die neue Schicht direkt zur lokalen Liste hinzu für sofortiges UI-Update
      shifts.value.push(response.data);
    } catch (error) {
      console.error('Fehler beim Erstellen der Schicht:', error);
    } finally {
      isLoading.value = false;
    }
  }

  async function updateShift(updatedShift: Shift) {
    isLoading.value = true;
    try {
      const response = await axios.put(`http://localhost:8080/api/shifts/${updatedShift.id}`, updatedShift);
      // Finde den Index der alten Schicht und ersetze sie
      const index = shifts.value.findIndex(s => s.id === updatedShift.id);
      if (index !== -1) {
        shifts.value[index] = response.data;
      }
    } catch (error) {
      console.error('Fehler beim Aktualisieren der Schicht:', error);
    } finally {
      isLoading.value = false;
    }
  }

  async function deleteShift(shiftId: string) {
    isLoading.value = true;
    try {
      await axios.delete(`http://localhost:8080/api/shifts/${shiftId}`);
      // Entferne die Schicht aus der lokalen Liste
      shifts.value = shifts.value.filter(s => s.id !== shiftId);
    } catch (error) {
      console.error('Fehler beim Löschen der Schicht:', error);
    } finally {
      isLoading.value = false;
    }
  }

  // Gib State und Actions zurück, damit Komponenten sie nutzen können
  return { shifts, isLoading, fetchShifts, createShift, updateShift, deleteShift };

})
