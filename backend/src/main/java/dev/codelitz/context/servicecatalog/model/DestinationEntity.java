package dev.codelitz.context.servicecatalog.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "service_destinations")
public class DestinationEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "service_id") private ServiceEntity service;
    @Column(nullable = false) private String name;
    @Column(nullable = false) private String label;
    @Column(nullable = false) private int position;
    @OneToMany(mappedBy = "destination", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    private List<DestinationLinkEntity> links = new ArrayList<>();

    public UUID getId() { return id; } public void setId(UUID value) { id = value; }
    public ServiceEntity getService() { return service; } public void setService(ServiceEntity value) { service = value; }
    public String getName() { return name; } public void setName(String value) { name = value; }
    public String getLabel() { return label; } public void setLabel(String value) { label = value; }
    public int getPosition() { return position; } public void setPosition(int value) { position = value; }
    public List<DestinationLinkEntity> getLinks() { return links; }
    public void addLink(DestinationLinkEntity link) { link.setDestination(this); links.add(link); }
}
