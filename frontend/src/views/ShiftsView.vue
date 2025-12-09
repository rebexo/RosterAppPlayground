<script setup lang="ts">
// ... dein bestehender Script-Teil bleibt unverändert ...
import { onMounted, ref } from 'vue';
import { useShiftStore, type Shift } from '@/stores/shift';

const shiftStore = useShiftStore();

const isModalOpen = ref(false);
const editingShift = ref<Partial<Shift>>({});
const isNew = ref(false);

onMounted(() => {
  shiftStore.fetchShifts();
});

function openCreateModal() {
  isNew.value = true;
  editingShift.value = {
    name: '',
    startTime: '00:00',
    endTime: '00:00'
  };
  isModalOpen.value = true;
}

function openEditModal(shift: Shift) {
  isNew.value = false;
  editingShift.value = { ...shift };
  isModalOpen.value = true;
}

function closeModal() {
  isModalOpen.value = false;
}

async function saveShift() {
  if (isNew.value) {
    await shiftStore.createShift(editingShift.value as Omit<Shift, 'id'>);
  } else {
    await shiftStore.updateShift(editingShift.value as Shift);
  }
  closeModal();
}

async function handleDelete(shiftId: string) {
  if (confirm('Bist du sicher, dass du diese Schicht löschen möchtest?')) {
    await shiftStore.deleteShift(shiftId);
  }
}
</script>

<template>
  <div>
    <h1>Deine Schichten</h1>
    <button @click="openCreateModal">Neue Schicht erstellen</button>

    <div v-if="shiftStore.isLoading">Lade Schichten...</div>
    <ul v-else-if="shiftStore.shifts.length > 0">
      <li v-for="shift in shiftStore.shifts" :key="shift.id" @click="openEditModal(shift)">
        <span>
          <strong>{{ shift.name }}</strong> ({{ shift.startTime }} - {{ shift.endTime }})
        </span>
        <button class="delete-btn" @click.stop="handleDelete(shift.id)">Löschen</button>
      </li>
    </ul>
    <p v-else>Du hast noch keine Schichten angelegt.</p>

    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h2>{{ isNew ? 'Neue Schicht erstellen' : 'Schicht bearbeiten' }}</h2>
        <form @submit.prevent="saveShift">
          <label>Name:</label>
          <input v-model="editingShift.name" required />

          <label>Startzeit (HH:mm):</label>
          <input type="time" v-model="editingShift.startTime" step="60" required />

          <label>Endzeit (HH:mm):</label>
          <input type="time" v-model="editingShift.endTime" step="60" required />

          <div class="modal-actions">
            <button type="button" @click="closeModal">Abbrechen</button>
            <button type="submit">Speichern</button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Dein bestehendes CSS bleibt unverändert */
ul { list-style: none; padding: 0; }
li {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border-bottom: 1px solid #eee;
  cursor: pointer;
}
li:hover { background-color: #f9f9f9; }
.delete-btn { background-color: #ffdddd; border: 1px solid #ffaaaa; }

.modal-overlay {
  position: fixed;
  top: 0; left: 0;
  width: 100%; height: 100%;
  background-color: rgba(0,0,0,0.5);
  display: flex;
  justify-content: center;
  align-items: center;
}
.modal-content {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  width: 90%;
  max-width: 500px;
}
.modal-actions { margin-top: 1rem; }
input { display: block; width: 100%; margin-bottom: 1rem; }
</style>
