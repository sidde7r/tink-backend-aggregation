package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ThreadPoolsConfiguration {
    @JsonProperty
    private int maxThreadsContextGeneration = 20;

    public int getMaxThreadsContextGeneration() {
        return maxThreadsContextGeneration;
    }
}
