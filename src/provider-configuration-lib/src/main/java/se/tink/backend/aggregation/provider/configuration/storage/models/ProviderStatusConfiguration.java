package se.tink.backend.aggregation.provider.configuration.storage.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.core.ProviderStatuses;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;


@Entity
@Table(name = "provider_status_configurations")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderStatusConfiguration {
    @Id
    private String providerName;
    @Enumerated(EnumType.STRING)
    private ProviderStatuses status;

    public String getProviderName() {
        return providerName;
    }

    public ProviderStatuses getStatus() {
        return status;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setStatus(ProviderStatuses status) {
        this.status = status;
    }
}
