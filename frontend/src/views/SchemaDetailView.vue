<script setup lang="ts">
import { onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import FullCalendar from '@fullcalendar/vue3';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid'; // Wichtig für die Wochen/Stundenansicht
import interactionPlugin from '@fullcalendar/interaction';
import { formatDateDE } from '@/utils/formatters';
// Für deutsche Lokalisierung im Kalender
import deLocale from '@fullcalendar/core/locales/de';

// --- Interfaces ---

// Aktualisiertes Interface passend zum neuen Backend DTO (CalculatedShiftDto)
interface Shift {
  title: string;
  // Die alten Felder date, startTime, endTime brauchen wir für den Kalender nicht mehr zwingend,
  // wenn das Backend jetzt volle ISO-Strings liefert.
  // Wir verlassen uns auf die neuen Felder:
  startDateTime: string; // ISO-String (z.B. "2023-11-01T22:00:00")
  endDateTime: string;   // ISO-String (z.B. "2023-11-02T06:00:00")
  baseShiftId: string;
}

interface Schema {
  id: string;
  name: string;
  startDate: string; // ISO-Datum "YYYY-MM-DD"
  endDate: string;   // ISO-Datum "YYYY-MM-DD"
}

// --- Setup ---
const route = useRoute();
const router = useRouter();
const schemaId = route.params.id as string;
const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api';

const schema = ref<Schema | null>(null);
const calendarEvents = ref<any[]>([]);
const isLoading = ref(true);
const error = ref<string | null>(null);

// --- Hilfsfunktion: Generiert eine konsistente Farbe aus einem String (UUID) ---
// Diese Funktion "hasht" den String und nutzt das Ergebnis für den HSL-Farbwert.
// Gleiche UUID = Gleiche Farbe. Unterschiedliche UUID = Wahrscheinlich unterschiedliche Farbe.
function stringToHslColor(str: string, saturation = 65, lightness = 45) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    // Einfacher String-Hash-Algorithmus
    hash = str.charCodeAt(i) + ((hash << 5) - hash);
  }
  // Den Hash auf 0-360 Grad für den Farbkreis mappen
  const hue = Math.abs(hash % 360);
  // HSL-String zurückgeben (z.B. "hsl(120, 65%, 45%)")
  // Wir nutzen eine etwas dunklere Helligkeit (45%), damit weiße Schrift gut lesbar ist.
  return `hsl(${hue}, ${saturation}%, ${lightness}%)`;
}

// --- FullCalendar Optionen ---
const calendarOptions = ref({
  plugins: [dayGridPlugin, timeGridPlugin, interactionPlugin],
  initialView: 'dayGridMonth', // Startansicht
  locale: deLocale, // Kalender auf Deutsch
  headerToolbar: {
    left: 'prev,next today',
    center: 'title',
    // Hier definieren wir die Umschalt-Buttons
    right: 'dayGridMonth,timeGridWeek'
  },
  buttonText: {
    today: 'Heute',
    month: 'Monat',
    week: 'Woche'
  },
  events: calendarEvents.value, // Wird reaktiv aktualisiert
  // Weitere Einstellungen für die Wochenansicht
  views: {
    timeGridWeek: {
      allDaySlot: false, // Keine "Ganztägig" Zeile oben, da Schichten Zeiten haben
      slotMinTime: '00:00:00', // Startzeit der Anzeige (optional anpassbar)
      slotMaxTime: '24:00:00', // Endzeit der Anzeige (optional anpassbar)
      nowIndicator: true, // Zeigt aktuelle Uhrzeit als rote Linie
    }
  },
  // WICHTIG: Wird später gesetzt, um den Kalender auf den Zeitraum des Schemas zu beschränken
  validRange: undefined as any,
  initialDate: undefined as any,
  height: 'auto', // Passt die Höhe automatisch an
  eventTimeFormat: { // Wie Zeiten auf den Events angezeigt werden
    hour: '2-digit',
    minute: '2-digit',
    meridiem: false,
    hour12: false
  },
  eventDisplay: 'block' // Sorgt dafür, dass Events als Blöcke mit Start/Ende angezeigt werden
});

// --- API Calls ---

const fetchSchema = async () => {
  try {
    const response = await axios.get<Schema>(`${baseURL}/schemas/${schemaId}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });
    schema.value = response.data;
  } catch (err) {
    console.error('Fehler beim Laden des Schemas:', err);
    error.value = 'Schema konnte nicht geladen werden.';
  }
};

const fetchShifts = async () => {
  try {
    // Dieser Endpunkt muss jetzt die DTOs mit startDateTime und endDateTime liefern
    const response = await axios.get<Shift[]>(`${baseURL}/schemas/${schemaId}/details`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('authToken')}` }
    });

    // Mapping der Backend-Daten auf FullCalendar Event-Objekte
    calendarEvents.value = response.data.map((shift: Shift) => {
      const color = stringToHslColor(shift.baseShiftId);


      // // --- NEU: Bestimme die CSS-Klasse basierend auf der Startzeit ---
      //
      // // Wir erstellen ein Date-Objekt aus dem ISO-String, um die Stunde zu bekommen
      // const startDate = new Date(shift.startDateTime);
      // const startHour = startDate.getHours();
      //
      // let shiftClass = 'day-shift'; // Standardklasse
      //
      // // Logik zur Zuweisung der Klasse basierend auf der Stunde
      // if (startHour >= 5 && startHour < 10) {
      //   shiftClass = 'early-shift'; // Früh: 05:00 - 09:59
      // } else if (startHour >= 10 && startHour < 16) {
      //   shiftClass = 'day-shift';   // Tag: 10:00 - 15:59
      // } else if (startHour >= 16 && startHour < 21) {
      //   shiftClass = 'late-shift';  // Spät: 16:00 - 20:59
      // } else {
      //   // Alles andere (vor 5 Uhr oder nach 21 Uhr) ist eine Nachtschicht
      //   shiftClass = 'night-shift';
      // }
      // // ---------------------------------------------------------------

      // HIER IST DIE ENTSCHEIDENDE ÄNDERUNG:
      // Wir nutzen direkt die vollständigen ISO-Strings vom Backend.
      // FullCalendar erkennt automatisch, wenn start und end an unterschiedlichen Tagen liegen.
      return {
        title: shift.title,
        start: shift.startDateTime,
        end: shift.endDateTime,
        backgroundColor: color,
        borderColor: color,
        textColor: '#ffffff'
      };
    });

    // Events im Kalender aktualisieren
    calendarOptions.value = { ...calendarOptions.value, events: calendarEvents.value };

  } catch (err) {
    console.error('Fehler beim Laden der Schichten:', err);
    error.value = 'Schichten konnten nicht geladen werden.';
  }
};

// --- Watcher & Lifecycle ---

// Sobald das Schema geladen ist, konfigurieren wir den Kalender-Zeitraum
watch(schema, (newSchema) => {
  if (newSchema) {
    // Berechne das Enddatum für validRange (FullCalendar braucht das Enddatum exklusiv, also +1 Tag)
    const endDateExclusive = new Date(newSchema.endDate);
    endDateExclusive.setDate(endDateExclusive.getDate() + 1);
    const validEndString = endDateExclusive.toISOString().split('T')[0];

    calendarOptions.value = {
      ...calendarOptions.value,
      // Setzt den Kalenderfokus auf den Start des Dienstplans
      initialDate: newSchema.startDate,
      // Beschränkt die Navigation auf den Zeitraum des Plans
      validRange: {
        start: newSchema.startDate,
        end: validEndString
      }
    };
  }
});

onMounted(async () => {
  isLoading.value = true;
  await Promise.all([fetchSchema(), fetchShifts()]);
  isLoading.value = false;
});
</script>

<template>
  <div class="schema-detail">
    <button class="btn-back" @click="router.push('/schemas')">← Zurück zur Übersicht</button>

    <div v-if="isLoading" class="loading">Lade Dienstplan...</div>
    <div v-else-if="error" class="error">{{ error }}</div>
    <div v-else-if="schema" class="content">
      <h1>{{ schema.name }}</h1>
      <p class="subtitle">
        Zeitraum: {{ formatDateDE(schema.startDate) }} – {{ formatDateDE(schema.endDate) }}
      </p>

      <div class="calendar-container">
        <FullCalendar :options="calendarOptions" class="full-calendar" />
      </div>
    </div>
  </div>
</template>

<style scoped>
.schema-detail {
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem;
}

.btn-back {
  background: none;
  border: none;
  color: #666;
  cursor: pointer;
  font-size: 1rem;
  margin-bottom: 1rem;
  padding: 0;
  text-decoration: underline;
}

.loading, .error {
  text-align: center;
  padding: 2rem;
  font-size: 1.2rem;
  color: #666;
}

.error {
  color: #d9534f;
}

.content h1 {
  margin-bottom: 0.5rem;
  color: var(--color-heading);
}

.subtitle {
  color: #666;
  margin-bottom: 2rem;
}

.calendar-container {
  background: white;
  padding: 1.5rem;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

/* FullCalendar spezifische Styles */
.full-calendar {
  /* Sorgt für eine gute Mindesthöhe in der Wochenansicht */
  min-height: 600px;
}

/* Optional: Styling für Events anpassen */
:deep(.fc-event) {
  border-radius: 4px;
  padding: 2px 4px;
  font-size: 0.9em;
  border: none;
  background-color: hsla(160, 100%, 37%, 1); /* Deine Primärfarbe */
  color: white;
}

/* Anpassung der Toolbar-Buttons */
:deep(.fc-button-primary) {
  background-color: #f0f0f0;
  border-color: #ccc;
  color: #333;
}
:deep(.fc-button-primary:hover), :deep(.fc-button-primary.fc-button-active) {
  background-color: hsla(160, 100%, 37%, 1);
  border-color: hsla(160, 100%, 37%, 1);
  color: white;
}
</style>
