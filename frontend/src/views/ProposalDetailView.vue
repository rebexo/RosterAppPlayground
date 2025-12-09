<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useProposalStore } from '@/stores/proposal'
import { useRouter } from 'vue-router'
import axios from 'axios'

const props = defineProps<{
  proposalId: string // Wird vom Router übergeben
}>()

const proposalStore = useProposalStore()
const router = useRouter()
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';
const isExporting = ref(false) // Ladeindikator für den Button

const downloadExcelExport = async () => {
  if (!proposalStore.currentProposal) return
  isExporting.value = true
  try {
    const response = await axios.get(`${baseURL}/export/proposal/${props.proposalId}/excel`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` },
      responseType: 'blob', // WICHTIG für Binärdaten
    })

    // Erstelle einen temporären Link im Browser, um den Download zu starten
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    // Versuche, den Dateinamen aus dem Header zu lesen, sonst Fallback
    const contentDisposition = response.headers['content-disposition']
    let fileName = 'Dienstplan_Schema.xlsx'
    if (contentDisposition) {
      const fileNameMatch = contentDisposition.match(/filename="(.+)"/)
      if (fileNameMatch.length === 2) fileName = fileNameMatch[1]
    }
    link.setAttribute('download', fileName)
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  } catch (err) {
    console.error('Fehler beim Excel-Export:', err)
    alert('Export fehlgeschlagen.')
  } finally {
    isExporting.value = false
  }
}

onMounted(() => {
  proposalStore.fetchProposalDetails(props.proposalId)
})
</script>

<template>
  <div class="proposal-details">

    <div class="header-actions" style="margin-bottom: 1rem; display: flex; gap: 1rem;">
      <button @click="router.push({ name: 'schema-list' })" class="btn-back" style="margin-bottom: 0;">
        &larr; Übersicht
      </button>
      <button @click="downloadExcelExport()" class="btn-primary" :disabled="isExporting || !proposalStore.currentProposal">
        <span v-if="isExporting">Erstelle Excel...</span>
        <span v-else>Als Excel exportieren</span>
      </button>
    </div>

    <div v-if="proposalStore.isLoading">Lade Vorschlag...</div>

    <div v-else-if="proposalStore.currentProposal">
      <h1>Dienstplan: {{ proposalStore.currentProposal.schemaName }}</h1>

      <!-- Mitarbeiter-Zusammenfassung -->
      <table class="summary-table">
        <thead>
          <tr>
            <th>Name Mitarbeiter</th>
            <th>Anzahl Zielschichten</th>
            <th>Anzahl tatsächliche Schichten</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="summary in proposalStore.currentProposal.employeeSummary"
            :key="summary.staffName"
          >
            <td>{{ summary.staffName }}</td>
            <td>{{ summary.targetCount }}</td>
            <td>{{ summary.actualCount }}</td>
          </tr>
        </tbody>
      </table>

      <!-- Detail-Tabelle der Schichten -->
      <table class="shifts-table">
        <thead>
          <tr>
            <th>Schicht-Datum</th>
            <th>Name der Schicht</th>
            <th>Position</th>
            <th>Mitarbeiter</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(shift, index) in proposalStore.currentProposal.assignedShifts" :key="index">
            <td>{{ shift.date }}</td>
            <td>{{ shift.shiftName }}</td>
            <td>{{ shift.positionName }}</td>
            <td>{{ shift.staffName ?? 'NICHT BESETZT' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else>
      <p>Vorschlag konnte nicht geladen werden.</p>
    </div>
  </div>
</template>

<style scoped>
.proposal-details {
  padding: 2rem;
  max-width: 1200px;
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

.btn-primary {
  padding: 0.5rem 1rem;
  background-color: hsla(160, 100%, 37%, 1);
  color: white;
  border: none;
  border-radius: 5px;
  font-weight: bold;
  cursor: pointer;
}
.btn-primary:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

table {
  width: 100%;
  border-collapse: collapse;
  margin-top: 2rem;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-radius: 8px;
  overflow: hidden;
}
th,
td {
  border: 1px solid #e0e0e0;
  padding: 12px 15px;
  text-align: left;
}
th {
  background-color: #f8f8f8;
  font-weight: bold;
}
tr:nth-child(even) {
  background-color: #f9f9f9;
}
</style>
