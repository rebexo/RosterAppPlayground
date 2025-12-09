<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <main class="public-main">
    <RouterView />
  </main>
</template>

<style scoped>
.public-main {
  padding: 2rem;
  max-width: 900px;
  margin: 0 auto;
}
</style>
