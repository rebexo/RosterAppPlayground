<script setup lang="ts">
import { ref, onMounted, watch } from 'vue';
import { useSchemaStore, type Schema } from '@/stores/schema';
import { useTemplateStore } from '@/stores/template';

const schemaStore = useSchemaStore();
const templateStore = useTemplateStore();

// Lokaler State für das Hauptformular
const newSchema = ref<Schema>({
  name: '',
  startDate: '',
  endDate: '',
  templateAssignments: []
});

// Lokaler State für das Unter-Formular zum Hinzufügen einer Zuweisung
const currentAssignment = ref({
  templateId: '',
  validFrom: '',
  validTo: ''
});

// Lade verfügbare Templates, um sie im Dropdown anzuzeigen
onMounted(() => {
  templateStore.fetchTemplates();
});

// Beobachte das Start- und Enddatum des Hauptformulars
watch(() => [newSchema.value.startDate, newSchema.value.endDate], ([newStart, newEnd]) => {
  // Setze die Werte für die Template-Zuweisung standardmäßig auf die des Plans
  currentAssignment.value.validFrom = newStart;
  currentAssignment.value.validTo = newEnd;
});

function addAssignment() {
  if (!currentAssignment.value.templateId || !currentAssignment.value.validFrom || !currentAssignment.value.validTo) {
    alert('Bitte alle Felder für die Zuweisung ausfüllen.');
    return;
  }
  newSchema.value.templateAssignments.push({ ...currentAssignment.value });
  // Unter-Formular zurücksetzen
  currentAssignment.value = { templateId: '', validFrom: '', validTo: '' };
}

function removeAssignment(index: number) {
  newSchema.value.templateAssignments.splice(index, 1);
}

function handleSubmit() {
  if (newSchema.value.templateAssignments.length === 0) {
    alert('Bitte mindestens ein Template zuweisen.');
    return;
  }
  schemaStore.createSchema(newSchema.value);
}

// Hilfsfunktion, um den Namen eines Templates anhand seiner ID zu finden
function getTemplateName(templateId: string): string {
  const tpl = templateStore.templates.find(t => t.id === templateId);
  return tpl ? tpl.name : 'Unbekanntes Template';
}
</script>

<template>
  <div>
    <h1>Neuen Dienstplan erstellen</h1>
    <form @submit.prevent="handleSubmit">
      <div class="form-group">
        <label>Name des Dienstplans</label>
        <input v-model="newSchema.name" placeholder="z.B. Dienstplan Dezember 2025" required />
      </div>
      <div class="form-group">
        <label>Startdatum</label>
        <input type="date" v-model="newSchema.startDate" required />
      </div>
      <div class="form-group">
        <label>Enddatum</label>
        <input type="date" v-model="newSchema.endDate" required />
      </div>

      <fieldset class="assignment-adder">
        <legend>Templates zuweisen</legend>
        <div class="form-group">
          <label>Template</label>
          <select v-model="currentAssignment.templateId">
            <option disabled value="">Template auswählen</option>
            <option v-for="template in templateStore.templates" :key="template.id" :value="template.id">
              {{ template.name }}
            </option>
          </select>
        </div>
        <div class="form-group">
          <label>Gültig von</label>
          <input type="date" v-model="currentAssignment.validFrom" />
        </div>
        <div class="form-group">
          <label>Gültig bis</label>
          <input type="date" v-model="currentAssignment.validTo" />
        </div>
        <button type="button" @click="addAssignment">Zuweisung hinzufügen</button>
      </fieldset>

      <div v-if="newSchema.templateAssignments.length > 0" class="assignments-list">
        <h4>Zugewiesene Templates</h4>
        <ul>
          <li v-for="(assignment, index) in newSchema.templateAssignments" :key="index">
            <span>
              <strong>{{ getTemplateName(assignment.templateId) }}</strong>
              ({{ assignment.validFrom }} bis {{ assignment.validTo }})
            </span>
            <button type="button" @click="removeAssignment(index)">Entfernen</button>
          </li>
        </ul>
      </div>

      <button type-="submit" class="submit-btn">Dienstplan erstellen</button>
    </form>
  </div>
</template>

<style scoped>
/* Füge hier Styles hinzu, um das Formular übersichtlich zu gestalten */
.form-group { margin-bottom: 1rem; }
.assignment-adder { border: 1px solid #ccc; padding: 1rem; margin-top: 2rem; border-radius: 8px; }
.assignments-list { margin-top: 1rem; }
.submit-btn { margin-top: 2rem; background-color: hsla(160, 100%, 37%, 1); color: white; }
</style>
