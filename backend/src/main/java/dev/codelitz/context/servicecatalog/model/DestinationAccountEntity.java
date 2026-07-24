package dev.codelitz.context.servicecatalog.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "destination_link_accounts")
public class DestinationAccountEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "destination_link_id") private DestinationLinkEntity link;
    @Column(nullable = false) private String label;
    @Column(name = "account_identifier", nullable = false) private String identifier;
    @Column(name = "authentication_method", nullable = false) private String authenticationMethod;
    @Column(nullable = false) private int position;

    public UUID getId() { return id; } public void setId(UUID value) { id = value; }
    public DestinationLinkEntity getLink() { return link; } public void setLink(DestinationLinkEntity value) { link = value; }
    public String getLabel() { return label; } public void setLabel(String value) { label = value; }
    public String getIdentifier() { return identifier; } public void setIdentifier(String value) { identifier = value; }
    public String getAuthenticationMethod() { return authenticationMethod; } public void setAuthenticationMethod(String value) { authenticationMethod = value; }
    public int getPosition() { return position; } public void setPosition(int value) { position = value; }
}
