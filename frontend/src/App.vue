<script setup lang="ts">
import { computed } from 'vue';
import { useRoute } from 'vue-router';
import DefaultLayout from './layouts/DefaultLayout.vue';
import PublicLayout from './layouts/PublicLayout.vue';

const route = useRoute();

// Entscheide basierend auf den Routen-Metadaten, welches Layout verwendet wird
const layout = computed(() => {
  if (route.meta.layout === 'Public') {
    return PublicLayout;
  }
  return DefaultLayout;
});
</script>

<template>
  <component :is="layout" />
</template>
