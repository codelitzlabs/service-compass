package dev.codelitz.context.servicecatalog.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "teams")
public class TeamEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true) private String name;
    @Column(nullable = false) private String description;
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "team_members", joinColumns = @JoinColumn(name = "team_id"))
    @Column(name = "member")
    private Set<String> members = new LinkedHashSet<>();
    protected TeamEntity() {}
    public TeamEntity(UUID id, String name, String description) { this.id = id; this.name = name; this.description = description; }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public void setDescription(String value) { description = value; }
    public Set<String> getMembers() { return members; }
    public void setMembers(Set<String> value) { members = value; }
}
