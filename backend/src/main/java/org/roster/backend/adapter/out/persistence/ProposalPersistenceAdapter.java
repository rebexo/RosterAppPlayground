package org.roster.backend.adapter.out.persistence;

import lombok.RequiredArgsConstructor;
import org.roster.backend.application.port.out.ProposalPort;
import org.roster.backend.domain.ScheduleProposal;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProposalPersistenceAdapter implements ProposalPort {

    private final ScheduleProposalRepository proposalRepository;

    @Override
    public ScheduleProposal save(ScheduleProposal proposal) {
        return proposalRepository.save(proposal);
    }

    @Override
    public Optional<ScheduleProposal> findById(UUID id) {
        return proposalRepository.findById(id);
    }

    @Override
    public Optional<ScheduleProposal> findLatestBySchemaId(UUID schemaId) {
        return proposalRepository.findLatestProposalBySchemaId(schemaId);
    }
}