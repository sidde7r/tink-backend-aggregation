package se.tink.backend.aggregation.configurations;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CoordinationConfiguration implements se.tink.libraries.discovery.CoordinationConfiguration {
    @JsonProperty
    private String hosts;
    @JsonProperty
    private int maxRetries = 5;
    @JsonProperty
    private int baseSleepTimeMs = 1000;

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public String getHosts() {
        return hosts;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

}
