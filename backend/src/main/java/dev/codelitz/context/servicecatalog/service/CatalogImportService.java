package dev.codelitz.context.servicecatalog.service;

import dev.codelitz.context.servicecatalog.dto.CatalogImportRequest;
import dev.codelitz.context.servicecatalog.dto.CatalogImportResponse;
import dev.codelitz.context.servicecatalog.dto.DestinationLinkRequest;
import dev.codelitz.context.servicecatalog.dto.DestinationAccountRequest;
import dev.codelitz.context.servicecatalog.dto.DestinationRequest;
import dev.codelitz.context.servicecatalog.dto.ServiceRequest;
import dev.codelitz.context.servicecatalog.dto.TeamRequest;
import java.util.LinkedHashSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CatalogImportService {
    private final TeamService teams;
    private final ServiceCatalogService services;

    public CatalogImportService(TeamService teams, ServiceCatalogService services) {
        this.teams = teams;
        this.services = services;
    }

    public CatalogImportResponse importCatalog(CatalogImportRequest request) {
        request.teams().forEach(teams::createOrUpdate);
        request.services().forEach(services::createOrUpdate);
        return new CatalogImportResponse(request.teams().size(), request.services().size());
    }

    @Transactional(readOnly = true)
    public CatalogImportRequest exportCatalog() {
        var exportedTeams = teams.findAll().stream()
            .map(team -> new TeamRequest(team.name(), team.description(), team.owners()))
            .toList();
        var exportedServices = services.findAll(null, org.springframework.data.domain.Pageable.unpaged()).stream()
            .map(service -> new ServiceRequest(
                service.name(),
                service.description(),
                service.owners(),
                service.teams().stream()
                    .map(team -> team.name())
                    .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)),
                service.lifecycle(),
                service.repositoryUrl(),
                service.tags(),
                service.destinations().stream()
                    .map(destination -> new DestinationRequest(
                        destination.name(),
                        destination.label(),
                        destination.links().stream()
                            .map(link -> new DestinationLinkRequest(
                                link.url(),
                                link.environment() == null ? null : link.environment().name(),
                                link.authenticationMethod(),
                                link.accountIdentifier(),
                                link.accessNotes(),
                                link.accessUrl(),
                                link.accounts().stream().map(account -> new DestinationAccountRequest(
                                    account.label(), account.identifier(), account.authenticationMethod())).toList()))
                            .toList()))
                    .toList()))
            .toList();
        return new CatalogImportRequest(exportedTeams, exportedServices);
    }
}
