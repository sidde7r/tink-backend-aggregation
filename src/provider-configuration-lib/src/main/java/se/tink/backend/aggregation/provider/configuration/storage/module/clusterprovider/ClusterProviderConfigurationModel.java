package se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterProviderConfigurationModel {
    @JsonProperty("cluster-provider-configuration")
    private List<ClusterProviderListModel> clusterProviderConfiguration;

    public List<ClusterProviderListModel> getClusters() {
        return clusterProviderConfiguration;
    }

}
