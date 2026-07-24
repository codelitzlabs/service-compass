package dev.codelitz.context.servicecatalog.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "environments")
public class EnvironmentEntity {
    @Id private UUID id;
    @Column(nullable = false, unique = true) private String name;
    @Column(nullable = false) private String color;
    protected EnvironmentEntity() {}
    public EnvironmentEntity(UUID id, String name, String color) { this.id = id; this.name = name; this.color = color; }
    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
}
