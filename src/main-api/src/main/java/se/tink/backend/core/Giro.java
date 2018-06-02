package se.tink.backend.core;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity(name = "giro_cache")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Giro {
    protected String accountNumber;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    protected long id;
    protected Date inserted;
    protected String name;
    protected String organizationId;
    @Enumerated(EnumType.STRING)
    protected SwedishGiroType type;

    public String getAccountNumber() {
        return accountNumber;
    }

    public long getId() {
        return id;
    }

    public Date getInserted() {
        return inserted;
    }

    public String getName() {
        return name;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public SwedishGiroType getType() {
        return type;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setInserted(Date inserted) {
        this.inserted = inserted;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public void setType(SwedishGiroType type) {
        this.type = type;
    }

}
