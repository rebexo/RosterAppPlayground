package org.roster.backend.application.port.in;

import java.io.IOException;
import java.util.UUID;

public interface iExportService {
    /**
     * Generiert eine Excel-Datei für einen bestimmten Dienstplan-Vorschlag.
     * @param proposalId Die ID des Vorschlags.
     * @return Das Excel-File als Byte-Array.
     * @throws IOException Wenn beim Erstellen der Datei ein Fehler auftritt.
     */
    byte[] generateProposalExcel(UUID proposalId) throws IOException;
}