package se.tink.backend.aggregation.provider.configuration.storage.module.clusterprovider;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClusterProviderListModel {
    @JsonProperty("clusterid")
    private String clusterId;

    @JsonProperty("providers")
    private List<String> providerName;

    public String getClusterId() {
        return clusterId;
    }

    public List<String> getProviderName() {
        return providerName;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public void setProviderName(List<String> providerName) {
        this.providerName = providerName;
    }
}
