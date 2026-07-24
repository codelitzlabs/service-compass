package dev.codelitz.context.servicecatalog.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "destination_links")
public class DestinationLinkEntity {
    @Id private UUID id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "destination_id") private DestinationEntity destination;
    @Column(nullable = false) private String url;
    @ManyToOne(fetch = FetchType.EAGER) @JoinColumn(name = "environment_id") private EnvironmentEntity environment;
    @Column(name = "authentication_method") private String authenticationMethod;
    @Column(name = "account_identifier") private String accountIdentifier;
    @Column(name = "access_notes") private String accessNotes;
    @Column(name = "access_url") private String accessUrl;
    @OneToMany(mappedBy = "link", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("position ASC")
    private List<DestinationAccountEntity> accounts = new ArrayList<>();
    @Column(nullable = false) private int position;

    public UUID getId() { return id; } public void setId(UUID value) { id = value; }
    public DestinationEntity getDestination() { return destination; } public void setDestination(DestinationEntity value) { destination = value; }
    public String getUrl() { return url; } public void setUrl(String value) { url = value; }
    public EnvironmentEntity getEnvironment() { return environment; } public void setEnvironment(EnvironmentEntity value) { environment = value; }
    public String getAuthenticationMethod() { return authenticationMethod; } public void setAuthenticationMethod(String value) { authenticationMethod = value; }
    public String getAccountIdentifier() { return accountIdentifier; } public void setAccountIdentifier(String value) { accountIdentifier = value; }
    public String getAccessNotes() { return accessNotes; } public void setAccessNotes(String value) { accessNotes = value; }
    public String getAccessUrl() { return accessUrl; } public void setAccessUrl(String value) { accessUrl = value; }
    public List<DestinationAccountEntity> getAccounts() { return accounts; }
    public void addAccount(DestinationAccountEntity account) { account.setLink(this); accounts.add(account); }
    public int getPosition() { return position; } public void setPosition(int value) { position = value; }
}
