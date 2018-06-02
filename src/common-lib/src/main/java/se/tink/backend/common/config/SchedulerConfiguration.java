package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SchedulerConfiguration {
    @JsonProperty
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }
}
