<script setup lang="ts">
import { ref, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import type { Ref } from 'vue';
import apiClient from '@/http-common.ts'

interface PublicSchema {
  name: string;
  startDate: string;
  endDate: string;
}

const route = useRoute();
const linkId = route.params.linkId as string;

const schema: Ref<PublicSchema | null> = ref(null);
const error = ref('');
const successMessage = ref('');
const isLoading = ref(true);

const staffName = ref('');
const comment = ref('');
const unavailableDates: Ref<Set<string>> = ref(new Set());

// Berechnet eine Liste aller Tage im Dienstplan
const days = computed(() => {
  if (!schema.value) return [];
  const dayList = [];
  let current = new Date(schema.value.startDate);
  const end = new Date(schema.value.endDate);

  // Zeitzonenprobleme vermeiden, indem wir in UTC arbeiten
  current.setUTCHours(0, 0, 0, 0);
  end.setUTCHours(0, 0, 0, 0);

  while (current <= end) {
    dayList.push(current.toISOString().split('T')[0]);
    current.setDate(current.getDate() + 1);
  }
  return dayList;
});

onMounted(async () => {
  try {
    const response = await apiClient.get(`/public/schemas/${linkId}`);
    schema.value = response.data;
  } catch (e: any) {
    error.value = e.response?.data || 'Der Link ist ungültig oder abgelaufen.';
  } finally {
    isLoading.value = false;
  }
});

function toggleDay(date: string) {
  if (unavailableDates.value.has(date)) {
    unavailableDates.value.delete(date);
  } else {
    unavailableDates.value.add(date);
  }
}

async function handleSubmit() {
  if (!staffName.value) {
    alert('Bitte gib deinen Namen an.');
    return;
  }

  const submissionData = {
    staffName: staffName.value,
    comment: comment.value,
    details: Array.from(unavailableDates.value).map(date => ({
      date: date,
      status: 'UNAVAILABLE' // Du könntest dies erweitern, um auch 'PREFERRED' zu ermöglichen
    }))
  };

  try {
    const response = await apiClient.post(`/public/schemas/${linkId}`, submissionData);
    successMessage.value = response.data;
  } catch (e: any) {
    error.value = e.response?.data || 'Ein Fehler ist aufgetreten.';
  }
}
</script>

<template>
  <div v-if="isLoading">Lade Daten...</div>
  <div v-else-if="error" class="message error">{{ error }}</div>
  <div v-else-if="successMessage" class="message success">{{ successMessage }}</div>
  <div v-else-if="schema">
    <h1>Verfügbarkeit eintragen</h1>
    <h2>{{ schema.name }} ({{ schema.startDate }} bis {{ schema.endDate }})</h2>

    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label for="staffName">Dein Name:</label>
        <input id="staffName" v-model="staffName" type="text" placeholder="Max Mustermann" required />
      </div>

      <div class="form-group">
        <label for="comment">Kommentar (optional):</label>
        <textarea id="comment" v-model="comment" placeholder="z.B. kann am Wochenende flexibel einspringen"></textarea>
      </div>

      <p>Klicke auf die Tage, an denen du <strong>nicht</strong> arbeiten kannst:</p>

      <div class="days-grid">
        <div
          v-for="day in days"
          :key="day"
          class="day-tile"
          :class="{ selected: unavailableDates.has(day) }"
          @click="toggleDay(day)"
        >
          {{ new Date(day + 'T00:00:00').toLocaleDateString('de-DE', { weekday: 'short', day: '2-digit', month: '2-digit' }) }}
        </div>
      </div>

      <button type="submit" class="submit-btn">Verfügbarkeit absenden</button>
    </form>
  </div>
</template>

<style scoped>
.form-group { margin-bottom: 2rem; }
.days-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  gap: 1rem;
  margin-top: 1rem;
}
.day-tile {
  padding: 1rem;
  border: 1px solid #ccc;
  border-radius: 8px;
  text-align: center;
  cursor: pointer;
  user-select: none;
  transition: background-color 0.2s, color 0.2s;
}
.day-tile:hover { background-color: #f0f0f0; }
.day-tile.selected {
  background-color: #d9534f;
  color: white;
  border-color: #d43f3a;
}
.submit-btn { margin-top: 2rem; font-size: 1.2rem; }
.message { padding: 2rem; text-align: center; font-size: 1.2rem; border-radius: 8px; }
.success { background-color: #dff0d8; color: #3c763d; }
.error { background-color: #f2dede; color: #a94442; }

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  font-weight: bold;
}
input, textarea {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid #ccc;
  border-radius: 4px;
}
textarea {
  min-height: 80px;
  resize: vertical;
}
</style>
