package se.tink.backend.aggregation.configuration.models;

import java.util.Set;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInformationServiceConfiguration {
    private Set<String> enabledClusters;

    public Set<String> getEnabledClusters() {
        return enabledClusters;
    }

    public void setEnabledClusters(Set<String> enabledClusters) {
        this.enabledClusters = enabledClusters;
    }
}
