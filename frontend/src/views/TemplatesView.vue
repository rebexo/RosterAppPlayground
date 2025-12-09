<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useTemplateStore, type Template } from '@/stores/template';
import { useShiftStore } from '@/stores/shift';

const templateStore = useTemplateStore();
const shiftStore = useShiftStore();

const isModalOpen = ref(false);
const editingTemplate = ref<Partial<Template>>({});
const isNew = ref(false);

const newShiftAssignment = ref({ weekday: 0, positionName: '', shiftId: '' });

onMounted(() => {
  templateStore.fetchTemplates();
  shiftStore.fetchShifts(); // Lade Schichten für das Dropdown im Modal
});

function openCreateModal() {
  isNew.value = true;
  editingTemplate.value = { name: '', description: '', shifts: [] };
  isModalOpen.value = true;
}

function openEditModal(template: Template) {
  isNew.value = false;
  // Tiefe Kopie, um reaktive Probleme mit verschachtelten Objekten zu vermeiden
  editingTemplate.value = JSON.parse(JSON.stringify(template));
  isModalOpen.value = true;
}

function closeModal() { isModalOpen.value = false; }

async function saveTemplate() {
  if (isNew.value) {
    await templateStore.createTemplate(editingTemplate.value as Template);
  } else {
    await templateStore.updateTemplate(editingTemplate.value as Template);
  }
  closeModal();
}

async function handleDelete(templateId: string) {
  if (confirm('Soll dieses Template wirklich gelöscht werden?')) {
    await templateStore.deleteTemplate(templateId);
  }
}

function addShiftToTemplate() {
  if (!newShiftAssignment.value.shiftId) return;
  editingTemplate.value.shifts?.push({ ...newShiftAssignment.value });
}

function removeShiftFromTemplate(index: number) {
  editingTemplate.value.shifts?.splice(index, 1);
}
</script>

<template>
  <div>
    <h1>Deine Templates</h1>
    <button @click="openCreateModal">Neues Template erstellen</button>

    <div v-if="templateStore.isLoading">Lade...</div>
    <ul v-else>
      <li v-for="template in templateStore.templates" :key="template.id" @click="openEditModal(template)">
        <span><strong>{{ template.name }}</strong><p>{{ template.description }}</p></span>
        <button @click.stop="handleDelete(template.id)">Löschen</button>
      </li>
    </ul>

    <div v-if="isModalOpen" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <h2>{{ isNew ? 'Neues Template' : 'Template bearbeiten' }}</h2>
        <form @submit.prevent="saveTemplate">
          <input v-model="editingTemplate.name" placeholder="Template-Name" required />
          <textarea v-model="editingTemplate.description" placeholder="Beschreibung"></textarea>

          <div class="shift-adder">
            <h4>Schichten hinzufügen</h4>
            <select v-model="newShiftAssignment.weekday">
              <option :value="0">Mo</option><option :value="1">Di</option><option :value="2">Mi</option>
              <option :value="3">Do</option><option :value="4">Fr</option><option :value="5">Sa</option><option :value="6">So</option>
            </select>
            <input v-model="newShiftAssignment.positionName" placeholder="Position" />
            <select v-model="newShiftAssignment.shiftId">
              <option disabled value="">Schicht auswählen</option>
              <option v-for="shift in shiftStore.shifts" :key="shift.id" :value="shift.id">{{ shift.name }}</option>
            </select>
            <button type="button" @click="addShiftToTemplate">+</button>
          </div>

          <ul class="assigned-shifts">
            <li v-for="(shift, index) in editingTemplate.shifts" :key="index">
              <span>Tag: {{ shift.weekday }}, Pos: {{ shift.positionName }}</span>
              <button type="button" @click="removeShiftFromTemplate(index)">X</button>
            </li>
          </ul>

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
/* Füge hier Styles hinzu, die denen aus ShiftsView.vue ähneln */
</style>
