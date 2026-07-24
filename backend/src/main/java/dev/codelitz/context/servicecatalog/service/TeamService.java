package dev.codelitz.context.servicecatalog.service;

import dev.codelitz.context.servicecatalog.dto.TeamRequest;
import dev.codelitz.context.servicecatalog.dto.TeamResponse;
import dev.codelitz.context.servicecatalog.exception.DuplicateTeamNameException;
import dev.codelitz.context.servicecatalog.exception.TeamHasServicesException;
import dev.codelitz.context.servicecatalog.exception.TeamNotFoundForDeletionException;
import dev.codelitz.context.servicecatalog.model.TeamEntity;
import dev.codelitz.context.servicecatalog.repository.ServiceRepository;
import dev.codelitz.context.servicecatalog.repository.TeamRepository;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TeamService {
    private final TeamRepository repository;
    private final ServiceRepository services;
    public TeamService(TeamRepository repository, ServiceRepository services) { this.repository = repository; this.services = services; }

    @Transactional(readOnly = true)
    public List<TeamResponse> findAll() { return repository.findAllByOrderByNameAsc().stream().map(this::response).toList(); }

    public TeamResponse create(TeamRequest request) {
        var name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) throw new DuplicateTeamNameException(name);
        var description = request.description() == null ? "" : request.description().trim();
        var team = new TeamEntity(UUID.randomUUID(), name, description);
        team.setMembers(normalize(request.owners()));
        return response(repository.save(team));
    }

    public TeamResponse createOrUpdate(TeamRequest request) {
        var name = request.name().trim();
        var description = request.description() == null ? "" : request.description().trim();
        var team = repository.findByNameIgnoreCase(name).orElseGet(() -> new TeamEntity(UUID.randomUUID(), name, description));
        team.setDescription(description);
        team.setMembers(normalize(request.owners()));
        return response(repository.save(team));
    }

    public void delete(UUID id) {
        var team = repository.findById(id).orElseThrow(() -> new TeamNotFoundForDeletionException(id));
        if (services.existsByTeams_Id(id)) throw new TeamHasServicesException(team.getName());
        repository.delete(team);
    }

    private TeamResponse response(TeamEntity team) { return new TeamResponse(team.getId(), team.getName(), team.getDescription(), team.getMembers()); }
    private LinkedHashSet<String> normalize(Set<String> values) {
        return values.stream().map(String::trim).filter(value -> !value.isBlank()).sorted(String.CASE_INSENSITIVE_ORDER)
            .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }
}
