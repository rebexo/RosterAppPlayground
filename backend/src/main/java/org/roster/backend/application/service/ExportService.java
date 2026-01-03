package org.roster.backend.application.service;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.in.iExportService;
import org.roster.backend.application.port.out.AvailabilityPort;
import org.roster.backend.application.port.out.ExcelExportPort;
import org.roster.backend.application.port.out.ProposalPort;
import org.roster.backend.domain.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;


/**
 * Service responsible for exporting roster proposal data to Excel files.
 * It facilitates the generation of Excel files that contain details of proposals
 * and their associated availability information.
 * This service integrates with various ports to access proposal and availability data
 * and delegates Excel file creation to the ExcelExportPort.
 */
@Service
@RequiredArgsConstructor
public class ExportService implements iExportService {
    //private static final Logger log = LoggerFactory.getLogger(ExportService.class);

    private final ProposalPort proposalPort;
    private final AvailabilityPort availabilityPort;
    private final ExcelExportPort excelExportPort;

    /**
     * Generates an Excel file containing the roster proposal based on the specified proposal ID.
     * Retrieves the proposal and associated availabilities, and constructs the Excel content.
     *
     * @param proposalId the unique identifier of the proposal to generate the Excel file for
     * @return a byte array representing the generated Excel file
     * @throws IOException if an error occurs during Excel file generation
     */
    @Transactional(readOnly = true)
    public byte[] generateProposalExcel(UUID proposalId) throws IOException {
        ScheduleProposal proposal = proposalPort.findById(proposalId)
                .orElseThrow(() -> new RuntimeException("Proposal not found"));

        List<AvailabilityEntry> availabilities = availabilityPort.findAllBySchemaId(proposal.getSchema().getId());

        return excelExportPort.generateRosterFile(proposal, availabilities);
        }
}