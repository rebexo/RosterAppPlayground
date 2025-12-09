package org.roster.backend.application.port.in;

import org.roster.backend.adapter.in.web.dto.ProposalDetailDto;

import java.util.Optional;
import java.util.UUID;

public interface iProposalService {
    ProposalDetailDto getProposalDetails(UUID proposalId);

    Optional<UUID> getLatestProposalIdBySchemaId(UUID schemaId);
}
