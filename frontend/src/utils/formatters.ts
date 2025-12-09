/**
 * Formatiert ein Datum (z.B. "2025-10-14") in das deutsche Format "14.10.2025".
 * @param dateString Das Datum im ISO-Format (YYYY-MM-DD).
 * @returns Das formatierte Datum als String.
 */
export function formatDateDE(dateString: string | null | undefined): string {
  if (!dateString) {
    return ''; // Gib einen leeren String zurück, falls kein Datum vorhanden ist
  }

  const date = new Date(dateString);
  // 'de-DE' sorgt für die deutsche Ländereinstellung
  const options: Intl.DateTimeFormatOptions = {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  };

  return new Intl.DateTimeFormat('de-DE', options).format(date);
}
