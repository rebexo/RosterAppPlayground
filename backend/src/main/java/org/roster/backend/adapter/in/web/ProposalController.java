package org.roster.backend.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.roster.backend.adapter.in.web.dto.ProposalDetailDto;
import org.roster.backend.application.service.ProposalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/proposals")
@RequiredArgsConstructor
public class ProposalController {

    private final ProposalService proposalService;

    @GetMapping("/{proposalId}")
    public ResponseEntity<ProposalDetailDto> getProposal(@PathVariable UUID proposalId) {
        try {
            ProposalDetailDto dto = proposalService.getProposalDetails(proposalId);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Endpunkt, um die ID des zuletzt generierten Schedule Proposals für ein spezifisches Schema abzurufen.
     */
    @GetMapping("/latest/bySchema/{schemaId}/id")
    public ResponseEntity<UUID> getLatestProposalIdBySchemaId(@PathVariable UUID schemaId) {
        return proposalService.getLatestProposalIdBySchemaId(schemaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

}