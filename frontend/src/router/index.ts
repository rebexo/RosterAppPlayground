import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth' // Importiere den Auth-Store

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/',
      name: 'dashboard',
      component: () => import('../views/DashboardView.vue'), // Deine Hauptseite nach dem Login
      meta: { requiresAuth: true } // Diese Route erfordert Authentifizierung
    },
    {
      path: '/templates',
      name: 'template-list',
      component: () => import('../views/TemplatesView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/shifts',
      name: 'shift-list',
      component: () => import('../views/ShiftsView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/schemas',
      name: 'schema-list',
      component: () => import('../views/SchemasView.vue'), // späti
      meta: { requiresAuth: true }
    },
    {
      path: '/schemas/new',
      name: 'schema-create',
      component: () => import('../views/SchemaCreateView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/schemas/:id',
      name: 'schema-details', // Endpunkt für spezifisches Schema
      component: () => import('../views/SchemaDetailView.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/schemas/:schemaId/availabilities',
      name: 'schema-availabilities',
      component: () => import('../views/SchemaAvailabilitiesView.vue'),
      props: true, // möglich, schemaId als Prop an die Komponente zu übergeben
      meta: { requiresAuth: true }
    },
    {
      path: '/proposals/:proposalId', // ID des Vorschlags, nicht des Schemas!
      name: 'proposal-details',
      component: () => import('../views/ProposalDetailView.vue'),
      props: true, // Übergibt 'proposalId' als Prop an die Komponente
      meta: { requiresAuth: true }
    },
    {
      path: '/submit/:linkId',
      name: 'submit-availability',
      component: () => import('../views/AvailabilitySubmissionView.vue'),
      meta: { layout: 'Public' }
    },
  ]
})

// Navigation Guard: Läuft vor jedem Routenwechsel
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    // Wenn die Seite einen Login erfordert und der User nicht eingeloggt ist -> zum Login umleiten
    next({ name: 'login' })
  } else {
    // Ansonsten den Wechsel zur neuen Seite erlauben
    next()
  }
})

export default router
