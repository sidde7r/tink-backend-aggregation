package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchConfiguration {
    @JsonProperty
    private String clusterName;
    @JsonProperty
    private boolean enabled;
    @JsonProperty
    private String hosts;
    @JsonProperty
    private boolean useHostSniffing = true;

    public String getClusterName() {
        return clusterName;
    }

    public String getHosts() {
        return hosts;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean useHostSniffing() {
        return useHostSniffing;
    }
}
