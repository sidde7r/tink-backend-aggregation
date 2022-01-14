package se.tink.backend.aggregation.configuration.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class CacheConfiguration {
    @JsonProperty private String hosts;
    @JsonProperty private boolean enabled = true;
    @JsonProperty private List<String> mirroredClusters;

    public boolean isEnabled() {
        return enabled;
    }

    public String getHosts() {
        return hosts;
    }

    public List<String> getReplicaHosts() {
        return mirroredClusters;
    }
}
