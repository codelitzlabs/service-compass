package dev.codelitz.context.servicecatalog.mapper;

import dev.codelitz.context.servicecatalog.dto.DestinationLinkResponse;
import dev.codelitz.context.servicecatalog.dto.DestinationAccountResponse;
import dev.codelitz.context.servicecatalog.dto.DestinationResponse;
import dev.codelitz.context.servicecatalog.dto.EnvironmentResponse;
import dev.codelitz.context.servicecatalog.dto.ServiceRequest;
import dev.codelitz.context.servicecatalog.dto.ServiceResponse;
import dev.codelitz.context.servicecatalog.dto.TeamSummary;
import dev.codelitz.context.servicecatalog.model.DestinationEntity;
import dev.codelitz.context.servicecatalog.model.ServiceEntity;
import org.springframework.stereotype.Component;

@Component
public class ServiceMapper {
    public ServiceResponse toResponse(ServiceEntity entity) {
        var teams = entity.getTeams().stream().map(team -> new TeamSummary(team.getId(), team.getName()))
            .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
        return new ServiceResponse(entity.getId(), entity.getName(), entity.getDescription(), entity.getOwners(), teams,
            entity.getLifecycle(), entity.getRepositoryUrl(), entity.getTags(), entity.getDestinations().stream().map(this::destination).toList(),
            entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public void updateBasics(ServiceRequest request, ServiceEntity entity) {
        entity.setName(request.name().trim());
        entity.setDescription(request.description().trim());
        entity.setLifecycle(request.lifecycle());
        entity.setRepositoryUrl(request.repositoryUrl().trim());
    }

    private DestinationResponse destination(DestinationEntity entity) {
        var links = entity.getLinks().stream().map(link -> {
            var environment = link.getEnvironment() == null ? null : new EnvironmentResponse(
                link.getEnvironment().getId(), link.getEnvironment().getName(), link.getEnvironment().getColor());
            var accounts = link.getAccounts().stream()
                .map(account -> new DestinationAccountResponse(account.getId(), account.getLabel(), account.getIdentifier(), account.getAuthenticationMethod()))
                .toList();
            if (accounts.isEmpty() && link.getAccountIdentifier() != null && !link.getAccountIdentifier().isBlank()) {
                accounts = java.util.List.of(new DestinationAccountResponse(null, "User", link.getAccountIdentifier(),
                    link.getAuthenticationMethod() == null ? "Not specified" : link.getAuthenticationMethod()));
            }
            return new DestinationLinkResponse(link.getId(), link.getUrl(), environment, link.getAuthenticationMethod(),
                link.getAccountIdentifier(), link.getAccessNotes(), link.getAccessUrl(), accounts);
        }).toList();
        return new DestinationResponse(entity.getId(), entity.getName(), entity.getLabel(), links);
    }
}
