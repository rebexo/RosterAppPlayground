<script setup lang="ts">
import { onMounted } from 'vue';
import { useTemplateStore } from '@/stores/template';
import { RouterLink } from 'vue-router';

const templateStore = useTemplateStore();

onMounted(() => {
  templateStore.fetchTemplates();
});
</script>

<template>
  <div>
    <h1>Deine Templates</h1>
    <RouterLink to="/templates/new">Neues Template erstellen</RouterLink>

    <div v-if="templateStore.isLoading">Lade Templates...</div>
    <ul v-else-if="templateStore.templates.length > 0">
      <li v-for="template in templateStore.templates" :key="template.id">
        <strong>{{ template.name }}</strong>
        <p>{{ template.description }}</p>
      </li>
    </ul>
    <p v-else>Du hast noch keine Templates erstellt.</p>
  </div>
</template>
