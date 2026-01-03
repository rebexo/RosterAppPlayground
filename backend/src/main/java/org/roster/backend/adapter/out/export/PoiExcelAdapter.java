package org.roster.backend.adapter.out.export;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.roster.backend.application.port.out.ExcelExportPort;
import org.roster.backend.domain.*;
import org.roster.backend.domain.enums.AvailabilityStatus;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Infrastructure adapter responsible for generating Excel files using Apache POI.
 * <p>
 * This class implements the {@link ExcelExportPort} and handles all technical details
 * of the .xlsx file format. It keeps the application core free from third-party
 * document processing libraries.
 * </p>
 */
@Component
public class PoiExcelAdapter implements ExcelExportPort {

    @Override
    public byte[] generateRosterFile(ScheduleProposal proposal, List<AvailabilityEntry> availabilities) {
        ScheduleSchema schema = proposal.getSchema();
        List<LocalDate> dates = schema.getStartDate().datesUntil(schema.getEndDate().plusDays(1)).toList();

        // 1. Daten vorbereiten (Transformation für die View)

        // Alle verwendeten Schicht-Typen (BaseShifts) sammeln und sortieren
        List<Shift> distinctBaseShifts = proposal.getProposalShifts().stream()
                .map(ScheduleProposalShift::getBaseShift)
                .distinct()
                .sorted(Comparator.comparing(Shift::getName))
                .toList();

        // Map: BaseShift-ID -> Spaltenindex (Start bei 1, da 0 = Datum)
        Map<UUID, Integer> shiftColumnMap = new HashMap<>();
        for (int i = 0; i < distinctBaseShifts.size(); i++) {
            shiftColumnMap.put(distinctBaseShifts.get(i).getId(), i + 1);
        }

        // Map: Datum -> (BaseShift-ID -> Name des Mitarbeiters)
        Map<LocalDate, Map<UUID, String>> assignmentsByDateAndShift = mapAssignments(proposal);

        // Map: Datum -> Liste der verfügbaren Mitarbeiter (die NICHT arbeiten und NICHT unavailable sind)
        Map<LocalDate, List<String>> availableStaffByDate = calculateDailyAvailability(dates, availabilities);


        // 2. Excel-Datei erstellen (Apache POI)
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Dienstplan " + schema.getName());

            // --- Styles definieren ---
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle textStyle = createTextStyle(workbook);

            // --- Header Zeile ---
            Row headerRow = sheet.createRow(0);
            createCell(headerRow, 0, "Datum", headerStyle);

            int colIdx = 1;
            for (Shift baseShift : distinctBaseShifts) {
                createCell(headerRow, colIdx++, baseShift.getName(), headerStyle);
            }

            // Letzte Spalte: Wer wäre noch verfügbar?
            int availabilityColIdx = colIdx;
            createCell(headerRow, availabilityColIdx, "Verfügbar", headerStyle);

            // --- Datenzeilen ---
            int rowIdx = 1;
            for (LocalDate date : dates) {
                Row row = sheet.createRow(rowIdx++);

                // Spalte 0: Datum
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(date);
                dateCell.setCellStyle(dateStyle);

                // Spalten 1 bis N: Schicht-Zuweisungen
                Map<UUID, String> daysAssignments = assignmentsByDateAndShift.getOrDefault(date, Collections.emptyMap());

                for (Shift baseShift : distinctBaseShifts) {
                    Integer colIndx = shiftColumnMap.get(baseShift.getId());
                    // Wenn jemand zugewiesen ist, Name eintragen, sonst leer
                    String assignedStaff = daysAssignments.getOrDefault(baseShift.getId(), "");

                    createCell(row, colIndx, assignedStaff, textStyle);
                }

                // Letzte Spalte: Verfügbare Mitarbeiter auflisten
                List<String> availableForDay = availableStaffByDate.getOrDefault(date, Collections.emptyList());
                String availableStr = String.join(", ", availableForDay);

                createCell(row, availabilityColIdx, availableStr, textStyle);
            }

            // --- Formatierung: Spaltenbreiten anpassen ---
            sheet.autoSizeColumn(0); // Datum
            for (int i = 1; i <= availabilityColIdx; i++) {
                // Mindestbreite setzen, da autoSize bei leeren Spalten zu schmal sein kann
                int width = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.max(width + 1000, 4000));
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            // Technische Exception in RuntimeException verpacken, damit Ports nicht 'throws IOException' brauchen
            throw new RuntimeException("Fehler beim Generieren der Excel-Datei", e);
        }
    }

    // --- helper methods ---

    private Map<LocalDate, Map<UUID, String>> mapAssignments(ScheduleProposal proposal) {
        Map<LocalDate, Map<UUID, String>> map = new HashMap<>();

        for (ScheduleProposalShift pShift : proposal.getProposalShifts()) {
            map.computeIfAbsent(pShift.getDate(), k -> new HashMap<>());

            String employeeName = pShift.getAssignedStaffName() != null ? pShift.getAssignedStaffName() : "?";

            // Falls (durch Fehler) mehrere Mitarbeiter auf derselben BaseShift sitzen, Namen verketten
            map.get(pShift.getDate())
                    .merge(pShift.getBaseShift().getId(), employeeName, (oldVal, newVal) -> oldVal + ", " + newVal);
        }
        return map;
    }

    private Map<LocalDate, List<String>> calculateDailyAvailability(List<LocalDate> dates, List<AvailabilityEntry> entries) {
        Map<LocalDate, List<String>> result = new HashMap<>();

        // Alle bekannten Mitarbeiternamen sammeln
        Set<String> allStaffNames = entries.stream()
                .map(AvailabilityEntry::getStaffName)
                .collect(Collectors.toSet());

        // Set aller "Nicht Verfügbar"-Kombinationen bauen (Performance O(1) Lookup)
        Set<String> unavailableCombinations = new HashSet<>();
        for (AvailabilityEntry entry : entries) {
            for (AvailabilityDetail detail : entry.getDetails()) {
                if (detail.getStatus() == AvailabilityStatus.UNAVAILABLE) {
                    unavailableCombinations.add(entry.getStaffName() + "|" + detail.getDate());
                }
            }
        }

        // Für jeden Tag prüfen, wer NICHT im unavailableSet ist
        for (LocalDate date : dates) {
            List<String> availableOnThisDay = new ArrayList<>();
            for (String staffName : allStaffNames) {
                String key = staffName + "|" + date;
                if (!unavailableCombinations.contains(key)) {
                    availableOnThisDay.add(staffName);
                }
            }
            Collections.sort(availableOnThisDay);
            result.put(date, availableOnThisDay);
        }
        return result;
    }

    // --- POI Style Helpers ---

    /**
     * Creates a new cell in the specified row at the given column index, sets its value,
     * and applies the provided style.
     *
     * @param row   the row in which the cell will be created
     * @param column the zero-based index of the column in the row for the cell
     * @param value  the value to be set in the created cell
     * @param style  the style to be applied to the created cell
     */
    private void createCell(Row row, int column, String value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    /**
     * Creates and returns a cell style specifically designed for header cells in an Excel sheet.
     * The style includes bold font, light green background, solid fill pattern,
     * and thin borders on the bottom and right sides.
     *
     * @param wb the workbook in which the style will be created
     * @return the created cell style configured for header cells
     */
    private CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    /**
     * Creates and returns a cell style configured for formatting date values in an Excel sheet.
     * The style includes a date format of "dd.MM.yyyy" and applies thin borders on the right
     * and bottom sides.
     *
     * @param wb the workbook in which the cell style will be created
     * @return the created cell style configured for date formatting
     */
    private CellStyle createDateStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setDataFormat(wb.createDataFormat().getFormat("dd.MM.yyyy"));
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    /**
     * Creates and returns a cell style configured for wrapping text and adding thin borders
     * to the right and bottom in an Excel sheet.
     *
     * @param wb the workbook in which the cell style will be created
     * @return the created cell style configured for text formatting
     */
    private CellStyle createTextStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }
}




//package org.roster.backend.adapter.out.export;
//
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.roster.backend.application.port.out.ExcelExportPort;
//import org.roster.backend.domain.*;
//import org.roster.backend.domain.enums.AvailabilityStatus;
//import org.springframework.stereotype.Component;
//
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.time.LocalDate;
//import java.util.*;
//import java.util.stream.Collectors;
//
///**
// * Infrastructure adapter responsible for generating Excel files using Apache POI.
// * <p>
// * This class implements the {@link ExcelExportPort} and handles all technical details
// * of the .xlsx file format. It keeps the application core free from third-party
// * document processing libraries.
// * </p>
// */
//
//@Component
//public class PoiExcelAdapter implements ExcelExportPort {
//
//
//    @Override
//    public byte[] generateRosterFile(ScheduleProposal proposal, List<AvailabilityEntry> availabilities) {
//
//        ScheduleSchema schema = proposal.getSchema();
//        List<LocalDate> dates = schema.getStartDate().datesUntil(schema.getEndDate().plusDays(1)).toList();
//
//        List<Shift> distinctBaseShifts = proposal.getProposalShifts().stream()
//                .map(ScheduleProposalShift::getBaseShift)
//                .distinct()
//                .sorted(Comparator.comparing(Shift::getName))
//                .toList();
//
//        // Map zum Finden von Spaltenindex einer Basis-Schicht-ID
//        Map<UUID, Integer> shiftColumnMap = new HashMap<>();
//        for (int i = 0; i < distinctBaseShifts.size(); i++) {
//            shiftColumnMap.put(distinctBaseShifts.get(i).getId(), i + 1); // Spalte 0 ist Datum, evtl später auf 2, damit Wochentag hinzugefügt werden kann?
//        }
//
//        // 2. Daten vorbereiten: Map von Datum -> (Map von BaseShiftId -> Mitarbeitername)
//        Map<LocalDate, Map<UUID, String>> assignmentsByDateAndShift = new HashMap<>();
//        for (ScheduleProposalShift pShift : proposal.getProposalShifts()) {
//            assignmentsByDateAndShift.computeIfAbsent(pShift.getDate(), k -> new HashMap<>());
//            String employeeName = pShift.getAssignedStaffName() != null ? pShift.getAssignedStaffName() : "NICHT BESETZT";
//            // Falls mehrere gleiche Schichten an einem Tag sind (z.B. 2x Frühschicht Kasse), Namen kombinieren
//            assignmentsByDateAndShift.get(pShift.getDate())
//                    .merge(pShift.getBaseShift().getId(), employeeName, (oldVal, newVal) -> oldVal + ", " + newVal);
//        }
//
//        // 3a. Alle Mitarbeiter finden, die einen AvailabilityEntry abgegeben haben
//        List<AvailabilityEntry> allEntries = availabilityPort.findAllBySchemaId(schema.getId());
//        Set<String> allStaffNames = allEntries.stream()
//                .map(AvailabilityEntry::getStaffName)
//                .collect(Collectors.toSet()); // Eindeutige Namen
//
//        // 3b. Ein Set aller "Nicht Verfügbar"-Kombinationen erstellen (Name + Datum)
//        Set<String> unavailableCombinations = new HashSet<>();
//        for (AvailabilityEntry entry : allEntries) {
//            for (AvailabilityDetail detail : entry.getDetails()) {
//                if (detail.getStatus() == AvailabilityStatus.UNAVAILABLE) {
//                    // Wir erstellen einen eindeutigen Schlüssel für Mitarbeiter & Datum
//                    unavailableCombinations.add(entry.getStaffName() + "|" + detail.getDate().toString());
//                }
//            }
//        }
//
//        // 3c. Verfügbarkeiten pro Tag berechnen
//        Map<LocalDate, List<String>> availableStaffByDate = new HashMap<>();
//        for (LocalDate date : dates) {
//            List<String> availableOnThisDay = new ArrayList<>();
//            for (String staffName : allStaffNames) {
//                // Prüfen, ob der Mitarbeiter für diesen Tag als UNAVAILABLE markiert ist
//                String key = staffName + "|" + date.toString();
//                if (!unavailableCombinations.contains(key)) {
//                    // Kein "Unavailable"-Eintrag gefunden -> Mitarbeiter ist verfügbar
//                    availableOnThisDay.add(staffName);
//                }
//            }
//            // Namen sortieren
//            Collections.sort(availableOnThisDay);
//            availableStaffByDate.put(date, availableOnThisDay);
//        }
//
//        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
//
//            Sheet sheet = workbook.createSheet("Vorschlag " + proposal.getSchema().getName());
//
//            // Styles (wie zuvor)
//            CellStyle headerStyle = workbook.createCellStyle();
//            Font headerFont = workbook.createFont();
//            headerFont.setBold(true);
//            headerStyle.setFont(headerFont);
//            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex()); // Andere Farbe zur Unterscheidung
//            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//            headerStyle.setBorderBottom(BorderStyle.THIN);
//
//            CellStyle dateStyle = workbook.createCellStyle();
//            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd.MM.yyyy"));
//            dateStyle.setBorderRight(BorderStyle.THIN);
//
//            CellStyle cellStyle = workbook.createCellStyle();
//            cellStyle.setBorderRight(BorderStyle.THIN);
//            cellStyle.setBorderBottom(BorderStyle.THIN);
//            cellStyle.setWrapText(true); // Wichtig, falls mehrere Namen in einer Zelle stehen
//
//            // --- Kopfzeile (Header) ---
//            Row headerRow = sheet.createRow(0);
//            createCell(headerRow, 0, "Datum", headerStyle);
//
//            int colIdx = 1;
//            for (Shift baseShift : distinctBaseShifts) {
//                createCell(headerRow, colIdx++, baseShift.getName(), headerStyle);
//            }
//
//            int availabilityColIdx = colIdx;
//            createCell(headerRow, availabilityColIdx, "Verfügbar (alle)", headerStyle);
//
//            // --- Datenzeilen (Tage) ---
//            int rowIdx = 1;
//            for (LocalDate date : dates) {
//                Row row = sheet.createRow(rowIdx++);
//
//                // Spalte 0: Datum
//                Cell dateCell = row.createCell(0);
//                dateCell.setCellValue(date);
//                dateCell.setCellStyle(dateStyle);
//
//                // Zuweisungen für diesen Tag
//                Map<UUID, String> daysAssignments = assignmentsByDateAndShift.getOrDefault(date, Collections.emptyMap());
//
//                // Spalten 1 bis N: Mitarbeiter eintragen
//                for (Shift baseShift : distinctBaseShifts) {
//                    Integer colIndx = shiftColumnMap.get(baseShift.getId());
//                    String assignedStaff = daysAssignments.getOrDefault(baseShift.getId(), "");
//
//                    Cell cell = row.createCell(colIndx);
//                    cell.setCellValue(assignedStaff);
//                    cell.setCellStyle(cellStyle);
//                }
//
//                List<String> availableForDay = availableStaffByDate.getOrDefault(date, Collections.emptyList());
//                String availableStr = String.join(", ", availableForDay);
//
//                Cell availCell = row.createCell(availabilityColIdx);
//                availCell.setCellValue(availableStr);
//                availCell.setCellStyle(cellStyle);
//            }
//
//            // Spaltenbreiten anpassen
//            sheet.autoSizeColumn(0); // Datumsspalte
//            for (int i = 1; i <= distinctBaseShifts.size(); i++) {
//                // Setze eine vernünftige Mindestbreite, autoSize ist manchmal zu eng bei kurzen Namen
//                sheet.setColumnWidth(i, Math.max(sheet.getColumnWidth(i), 4000));
//            }
//
//            workbook.write(out);
//            return out.toByteArray();
//        } catch (IOException e) {
//            throw new RuntimeException("Fehler bei Excel-Generierung", e);
//        }
//    }
//
//    // Hilfsmethode zum Erstellen von Zellen mit Style
//    private void createCell(Row row, int column, String value, CellStyle style) {
//        Cell cell = row.createCell(column);
//        cell.setCellValue(value);
//        cell.setCellStyle(style);
//    }
//
//    // Hilfsmethode: Berechnet, wer an welchem Tag verfügbar ist
//    private Map<LocalDate, List<String>> calculateDailyAvailability(List<LocalDate> dates, List<AvailabilityEntry> entries) {
//        Map<LocalDate, List<String>> map = new HashMap<>();
//        for (LocalDate date : dates) {
//            map.put(date, new ArrayList<>());
//        }
//
//        for (AvailabilityEntry entry : entries) {
//            for (AvailabilityDetail detail : entry.getDetails()) {
//                if (map.containsKey(detail.getDate()) &&
//                        (detail.getStatus() != AvailabilityStatus.UNAVAILABLE)) {
//                    map.get(detail.getDate()).add(entry.getStaffName());
//                }
//            }
//        }
//        return map;
//    }
//}
