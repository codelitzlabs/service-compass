package dev.codelitz.context.servicecatalog.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "services")
public class ServiceEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true) private String name;
    @Column(nullable = false) private String description;
    @Column(nullable = false) private String lifecycle;
    @Column(name = "repository_url", nullable = false) private String repositoryUrl;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "service_owners", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "owner")
    private Set<String> owners = new LinkedHashSet<>();
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "service_tags", joinColumns = @JoinColumn(name = "service_id"))
    @Column(name = "tag")
    private Set<String> tags = new LinkedHashSet<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "service_teams", joinColumns = @JoinColumn(name = "service_id"), inverseJoinColumns = @JoinColumn(name = "team_id"))
    private Set<TeamEntity> teams = new LinkedHashSet<>();
    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    private List<DestinationEntity> destinations = new ArrayList<>();
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;

    public UUID getId() { return id; } public void setId(UUID id) { this.id = id; }
    public String getName() { return name; } public void setName(String name) { this.name = name; }
    public String getDescription() { return description; } public void setDescription(String value) { description = value; }
    public String getLifecycle() { return lifecycle; } public void setLifecycle(String value) { lifecycle = value; }
    public String getRepositoryUrl() { return repositoryUrl; } public void setRepositoryUrl(String value) { repositoryUrl = value; }
    public Set<String> getOwners() { return owners; } public void setOwners(Set<String> value) { owners = value; }
    public Set<String> getTags() { return tags; } public void setTags(Set<String> value) { tags = value; }
    public Set<TeamEntity> getTeams() { return teams; } public void setTeams(Set<TeamEntity> value) { teams = value; }
    public List<DestinationEntity> getDestinations() { return destinations; }
    public void replaceDestinations(List<DestinationEntity> values) { destinations.clear(); values.forEach(this::addDestination); }
    public void addDestination(DestinationEntity destination) { destination.setService(this); destinations.add(destination); }
    public Instant getCreatedAt() { return createdAt; } public void setCreatedAt(Instant value) { createdAt = value; }
    public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant value) { updatedAt = value; }
}
