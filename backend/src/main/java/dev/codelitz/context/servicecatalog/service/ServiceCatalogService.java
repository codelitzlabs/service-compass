package dev.codelitz.context.servicecatalog.service;

import dev.codelitz.context.servicecatalog.dto.DestinationRequest;
import dev.codelitz.context.servicecatalog.dto.EnvironmentResponse;
import dev.codelitz.context.servicecatalog.dto.ServiceRequest;
import dev.codelitz.context.servicecatalog.dto.ServiceResponse;
import dev.codelitz.context.servicecatalog.exception.DuplicateServiceNameException;
import dev.codelitz.context.servicecatalog.exception.ServiceNotFoundException;
import dev.codelitz.context.servicecatalog.exception.TeamNotFoundException;
import dev.codelitz.context.servicecatalog.mapper.ServiceMapper;
import dev.codelitz.context.servicecatalog.model.DestinationEntity;
import dev.codelitz.context.servicecatalog.model.DestinationAccountEntity;
import dev.codelitz.context.servicecatalog.model.DestinationLinkEntity;
import dev.codelitz.context.servicecatalog.model.EnvironmentEntity;
import dev.codelitz.context.servicecatalog.model.ServiceEntity;
import dev.codelitz.context.servicecatalog.model.TeamEntity;
import dev.codelitz.context.servicecatalog.repository.EnvironmentRepository;
import dev.codelitz.context.servicecatalog.repository.ServiceRepository;
import dev.codelitz.context.servicecatalog.repository.TeamRepository;
import java.time.*;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ServiceCatalogService {
    private static final List<String> COLORS = List.of("green", "blue", "amber", "violet", "rose", "slate");
    private final ServiceRepository repository;
    private final EnvironmentRepository environments;
    private final TeamRepository teams;
    private final ServiceMapper mapper;
    private final Clock clock;

    @Autowired public ServiceCatalogService(ServiceRepository repository, EnvironmentRepository environments, TeamRepository teams, ServiceMapper mapper) {
        this(repository, environments, teams, mapper, Clock.systemUTC());
    }
    ServiceCatalogService(ServiceRepository repository, EnvironmentRepository environments, TeamRepository teams, ServiceMapper mapper, Clock clock) {
        this.repository = repository; this.environments = environments; this.teams = teams; this.mapper = mapper; this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Page<ServiceResponse> findAll(String query, Pageable pageable) {
        var page = query == null || query.isBlank() ? repository.findAll(pageable) : repository.search(query.trim(), pageable);
        return page.map(mapper::toResponse);
    }
    @Transactional(readOnly = true) public ServiceResponse findById(UUID id) { return mapper.toResponse(get(id)); }
    @Transactional(readOnly = true) public List<EnvironmentResponse> findEnvironments() {
        return environments.findAllByOrderByNameAsc().stream().map(e -> new EnvironmentResponse(e.getId(), e.getName(), e.getColor())).toList();
    }

    public ServiceResponse create(ServiceRequest request) {
        ensureUniqueName(request.name(), null);
        var entity = new ServiceEntity(); entity.setId(UUID.randomUUID()); mapper.updateBasics(request, entity);
        applyCollections(request, entity);
        var now = Instant.now(clock); entity.setCreatedAt(now); entity.setUpdatedAt(now);
        return mapper.toResponse(repository.save(entity));
    }
    public ServiceResponse update(UUID id, ServiceRequest request) {
        ensureUniqueName(request.name(), id);
        var entity = get(id); mapper.updateBasics(request, entity); applyCollections(request, entity); entity.setUpdatedAt(Instant.now(clock));
        return mapper.toResponse(repository.save(entity));
    }
    public ServiceResponse createOrUpdate(ServiceRequest request) {
        return repository.findByNameIgnoreCase(request.name().trim())
            .map(existing -> update(existing.getId(), request))
            .orElseGet(() -> create(request));
    }
    public void delete(UUID id) { repository.delete(get(id)); }

    private void applyCollections(ServiceRequest request, ServiceEntity entity) {
        entity.setOwners(normalize(request.owners())); entity.setTags(normalize(request.tags()));
        var assignedTeams = new LinkedHashSet<TeamEntity>();
        request.teams().forEach(name -> assignedTeams.add(teams.findByNameIgnoreCase(name.trim())
            .orElseThrow(() -> new TeamNotFoundException(name))));
        entity.setTeams(assignedTeams);
        var destinations = new ArrayList<DestinationEntity>();
        var inputs = request.destinations() == null ? List.<DestinationRequest>of() : request.destinations();
        for (int i = 0; i < inputs.size(); i++) {
            var input = inputs.get(i); var destination = new DestinationEntity();
            destination.setId(UUID.randomUUID()); destination.setName(input.name().trim()); destination.setLabel(input.label().trim());
            destination.setPosition(i);
            for (int linkPosition = 0; linkPosition < input.links().size(); linkPosition++) {
                var linkInput = input.links().get(linkPosition); var link = new DestinationLinkEntity();
                link.setId(UUID.randomUUID()); link.setUrl(linkInput.url().trim()); link.setPosition(linkPosition);
                link.setAuthenticationMethod(optional(linkInput.authenticationMethod()));
                link.setAccountIdentifier(optional(linkInput.accountIdentifier()));
                link.setAccessNotes(optional(linkInput.accessNotes()));
                link.setAccessUrl(optional(linkInput.accessUrl()));
                var accountInputs = linkInput.accounts() == null ? java.util.List.<dev.codelitz.context.servicecatalog.dto.DestinationAccountRequest>of() : linkInput.accounts();
                if (accountInputs.isEmpty() && linkInput.accountIdentifier() != null && !linkInput.accountIdentifier().isBlank()) {
                    accountInputs = java.util.List.of(new dev.codelitz.context.servicecatalog.dto.DestinationAccountRequest("User", linkInput.accountIdentifier(), linkInput.authenticationMethod()));
                }
                for (int accountPosition = 0; accountPosition < accountInputs.size(); accountPosition++) {
                    var accountInput = accountInputs.get(accountPosition); var account = new DestinationAccountEntity();
                    account.setId(UUID.randomUUID()); account.setLabel(accountInput.label().trim());
                    account.setIdentifier(accountInput.identifier().trim());
                    account.setAuthenticationMethod(authenticationMethod(accountInput.authenticationMethod(), linkInput.authenticationMethod()));
                    account.setPosition(accountPosition);
                    link.addAccount(account);
                }
                if (linkInput.environment() != null && !linkInput.environment().isBlank()) link.setEnvironment(findOrCreateEnvironment(linkInput.environment()));
                destination.addLink(link);
            }
            destinations.add(destination);
        }
        entity.replaceDestinations(destinations);
    }
    private EnvironmentEntity findOrCreateEnvironment(String rawName) {
        var name = rawName.trim();
        return environments.findByNameIgnoreCase(name).orElseGet(() -> environments.save(
            new EnvironmentEntity(UUID.randomUUID(), name, COLORS.get(Math.floorMod(name.toLowerCase().hashCode(), COLORS.size())))));
    }
    private LinkedHashSet<String> normalize(Collection<String> values) {
        var result = new LinkedHashSet<String>();
        if (values != null) values.stream().map(String::trim).filter(v -> !v.isBlank()).distinct().sorted(String.CASE_INSENSITIVE_ORDER).forEach(result::add);
        return result;
    }
    private String optional(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private String authenticationMethod(String accountMethod, String legacyMethod) {
        var method = optional(accountMethod);
        return method != null ? method : Optional.ofNullable(optional(legacyMethod)).orElse("Not specified");
    }
    private void ensureUniqueName(String name, UUID id) {
        boolean exists = id == null ? repository.existsByNameIgnoreCase(name) : repository.existsByNameIgnoreCaseAndIdNot(name, id);
        if (exists) throw new DuplicateServiceNameException(name);
    }
    private ServiceEntity get(UUID id) { return repository.findById(id).orElseThrow(() -> new ServiceNotFoundException(id)); }
}
