package se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProviderSpecificationModel {
    @JsonProperty("clusterid")
    private String clusterId;
    @JsonProperty("provider-configuration")
    private List<ProviderConfiguration> providerSpecificConfiguration;

    public String getClusterId() {
        return clusterId;
    }

    public List<ProviderConfiguration> getProviderSpecificConfiguration() {
        return providerSpecificConfiguration;
    }
}
