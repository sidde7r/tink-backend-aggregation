package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ExcludedDebugClusters {

    @JsonProperty private List<String> excludedClusters;

    public ExcludedDebugClusters() {}

    public List<String> getExcludedClusters() {
        return excludedClusters;
    }

    public void setExcludedClusters(List<String> excludedClusters) {
        this.excludedClusters = excludedClusters;
    }
}
