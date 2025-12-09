package org.roster.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.roster.backend.application.port.in.iExportService;
import org.roster.backend.application.port.out.AvailabilityPort;
import org.roster.backend.application.port.out.ProposalPort;
import org.roster.backend.domain.*;
import org.roster.backend.domain.enums.AvailabilityStatus;
import org.roster.backend.adapter.out.persistence.AvailabilityEntryRepository;
import org.roster.backend.adapter.out.persistence.ScheduleProposalRepository;
import org.roster.backend.adapter.out.persistence.ShiftRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExportService implements iExportService {
    //private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final ProposalPort proposalPort;
    private final AvailabilityPort availabilityPort;

    /**
     * Generiert einen Excel-Export für einen spezifischen Vorschlag (gefüllter Plan).
     */
    @Transactional(readOnly = true)
    public byte[] generateProposalExcel(UUID proposalId) throws IOException {
        ScheduleProposal proposal = proposalPort.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        ScheduleSchema schema = proposal.getSchema();
        List<LocalDate> dates = schema.getStartDate().datesUntil(schema.getEndDate().plusDays(1)).toList();

        // 1. alle distinct BaseShifts 'raussuchen
        List<Shift> distinctBaseShifts = proposal.getProposalShifts().stream()
                .map(ScheduleProposalShift::getBaseShift)
                .distinct()
                .sorted(Comparator.comparing(Shift::getName))
                .toList();

        // Map zum Finden von Spaltenindex einer Basis-Schicht-ID
        Map<UUID, Integer> shiftColumnMap = new HashMap<>();
        for (int i = 0; i < distinctBaseShifts.size(); i++) {
            shiftColumnMap.put(distinctBaseShifts.get(i).getId(), i + 1); // Spalte 0 ist Datum, evtl später auf 2, damit Wochentag hinzugefügt werden kann?
        }

        // 2. Daten vorbereiten: Map von Datum -> (Map von BaseShiftId -> Mitarbeitername)
        Map<LocalDate, Map<UUID, String>> assignmentsByDateAndShift = new HashMap<>();
        for (ScheduleProposalShift pShift : proposal.getProposalShifts()) {
            assignmentsByDateAndShift.computeIfAbsent(pShift.getDate(), k -> new HashMap<>());
            String employeeName = pShift.getAssignedStaffName() != null ? pShift.getAssignedStaffName() : "NICHT BESETZT";
            // Falls mehrere gleiche Schichten an einem Tag sind (z.B. 2x Frühschicht Kasse), Namen kombinieren
            assignmentsByDateAndShift.get(pShift.getDate())
                    .merge(pShift.getBaseShift().getId(), employeeName, (oldVal, newVal) -> oldVal + ", " + newVal);
        }

        // 3a. Alle Mitarbeiter finden, die einen AvailabilityEntry abgegeben haben
        List<AvailabilityEntry> allEntries = availabilityPort.findAllBySchemaId(schema.getId());
        Set<String> allStaffNames = allEntries.stream()
                .map(AvailabilityEntry::getStaffName)
                .collect(Collectors.toSet()); // Eindeutige Namen

        // 3b. Ein Set aller "Nicht Verfügbar"-Kombinationen erstellen (Name + Datum)
        Set<String> unavailableCombinations = new HashSet<>();
        for (AvailabilityEntry entry : allEntries) {
            for (AvailabilityDetail detail : entry.getDetails()) {
                if (detail.getStatus() == AvailabilityStatus.UNAVAILABLE) {
                    // Wir erstellen einen eindeutigen Schlüssel für Mitarbeiter & Datum
                    unavailableCombinations.add(entry.getStaffName() + "|" + detail.getDate().toString());
                }
            }
        }

        // 3c. Verfügbarkeiten pro Tag berechnen
        Map<LocalDate, List<String>> availableStaffByDate = new HashMap<>();
        for (LocalDate date : dates) {
            List<String> availableOnThisDay = new ArrayList<>();
            for (String staffName : allStaffNames) {
                // Prüfen, ob der Mitarbeiter für diesen Tag als UNAVAILABLE markiert ist
                String key = staffName + "|" + date.toString();
                if (!unavailableCombinations.contains(key)) {
                    // Kein "Unavailable"-Eintrag gefunden -> Mitarbeiter ist verfügbar
                    availableOnThisDay.add(staffName);
                }
            }
            // Namen sortieren
            Collections.sort(availableOnThisDay);
            availableStaffByDate.put(date, availableOnThisDay);
        }


        // 3. Excel Workbook erstellen
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Vorschlag " + schema.getName());

            // Styles (wie zuvor)
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex()); // Andere Farbe zur Unterscheidung
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd.MM.yyyy"));
            dateStyle.setBorderRight(BorderStyle.THIN);

            CellStyle cellStyle = workbook.createCellStyle();
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setWrapText(true); // Wichtig, falls mehrere Namen in einer Zelle stehen

            // --- Kopfzeile (Header) ---
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "Datum", headerStyle);

            int colIdx = 1;
            for (Shift baseShift : distinctBaseShifts) {
                createCell(headerRow, colIdx++, baseShift.getName(), headerStyle);
            }

            int availabilityColIdx = colIdx;
            createCell(headerRow, availabilityColIdx, "Verfügbar (alle)", headerStyle);

            // --- Datenzeilen (Tage) ---
            int rowIdx = 1;
            for (LocalDate date : dates) {
                Row row = sheet.createRow(rowIdx++);

                // Spalte 0: Datum
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(date);
                dateCell.setCellStyle(dateStyle);

                // Zuweisungen für diesen Tag
                Map<UUID, String> daysAssignments = assignmentsByDateAndShift.getOrDefault(date, Collections.emptyMap());

                // Spalten 1 bis N: Mitarbeiter eintragen
                for (Shift baseShift : distinctBaseShifts) {
                    Integer colIndx = shiftColumnMap.get(baseShift.getId());
                    String assignedStaff = daysAssignments.getOrDefault(baseShift.getId(), "");

                    Cell cell = row.createCell(colIndx);
                    cell.setCellValue(assignedStaff);
                    cell.setCellStyle(cellStyle);
                }

                List<String> availableForDay = availableStaffByDate.getOrDefault(date, Collections.emptyList());
                String availableStr = String.join(", ", availableForDay);

                Cell availCell = row.createCell(availabilityColIdx);
                availCell.setCellValue(availableStr);
                availCell.setCellStyle(cellStyle);
            }

            // Spaltenbreiten anpassen
            sheet.autoSizeColumn(0); // Datumsspalte
            for (int i = 1; i <= distinctBaseShifts.size(); i++) {
                // Setze eine vernünftige Mindestbreite, autoSize ist manchmal zu eng bei kurzen Namen
                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 4000));
            }

            workbook.write(out);
            return out.toByteArray();
        }
    }

    // Hilfsmethode zum Erstellen von Zellen mit Style
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    // Hilfsmethode: Berechnet, wer an welchem Tag verfügbar ist
    private Map<LocalDate, List<String>> calculateDailyAvailability(List<LocalDate> dates, List<AvailabilityEntry> entries) {
        Map<LocalDate, List<String>> map = new HashMap<>();
        for (LocalDate date : dates) {
            map.put(date, new ArrayList<>());
        }

        for (AvailabilityEntry entry : entries) {
            for (AvailabilityDetail detail : entry.getDetails()) {
                if (map.containsKey(detail.getDate()) &&
                        (detail.getStatus() != AvailabilityStatus.UNAVAILABLE)) {
                    map.get(detail.getDate()).add(entry.getStaffName());
                }
            }
        }
        return map;
    }
}