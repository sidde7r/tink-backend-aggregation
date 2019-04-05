package se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationStorage;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderSpecificationModel {
    @JsonProperty("clusterid")
    private String clusterId;
    @JsonProperty("provider-configuration")
    private List<ProviderConfigurationStorage> providerSpecificConfiguration;

    public String getClusterId() {
        return clusterId;
    }

    public List<ProviderConfigurationStorage> getProviderSpecificConfiguration() {
        return providerSpecificConfiguration;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public void setProviderSpecificConfiguration(List<ProviderConfigurationStorage> providerSpecificConfiguration) {
        this.providerSpecificConfiguration = providerSpecificConfiguration;
    }
}
