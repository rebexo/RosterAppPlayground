package org.roster.backend.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.in.iExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    private final iExportService exportService;

    @GetMapping("/proposal/{proposalId}/excel")
    public ResponseEntity<byte[]> exportProposalToExcel(@PathVariable UUID proposalId) {
        try {
            // Rufe die NEUE Methode auf
            byte[] excelContent = exportService.generateProposalExcel(proposalId);

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"));
            // Dateiname anpassen
            String filename = "Dienstplan_Vorschlag_" + timestamp + ".xlsx";

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(excelContent);

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

}