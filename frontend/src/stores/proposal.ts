import { defineStore } from 'pinia';
import { ref } from 'vue';
import type { Ref } from 'vue';
import axios from 'axios';

// Interfaces, die die DTOs spiegeln
interface AssignedShift {
  date: string;
  shiftName: string;
  positionName: string;
  staffName: string;
}

interface EmployeeSummary {
  staffName: string;
  targetCount: number;
  actualCount: number;
}

export interface ProposalDetail {
  schemaName: string;
  employeeSummary: EmployeeSummary[];
  assignedShifts: AssignedShift[];
}

export const useProposalStore = defineStore('proposal', () => {
  const currentProposal: Ref<ProposalDetail | null> = ref(null);
  const isLoading = ref(false);
  const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

  async function fetchProposalDetails(proposalId: string) {
    isLoading.value = true;
    currentProposal.value = null;
    try {
      const response = await axios.get<ProposalDetail>(`${baseURL}/proposals/${proposalId}`, {
        headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
      });
      currentProposal.value = response.data;
    } catch (error) {
      console.error('Fehler beim Laden des Vorschlags:', error);
    } finally {
      isLoading.value = false;
    }
  }

  return { currentProposal, isLoading, fetchProposalDetails };
});
