package se.tink.backend.aggregation.configuration.integrations.abnamro;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AbnAmroAccountUpdatesConfiguration {
    @JsonProperty private boolean enabled = false;

    @JsonProperty private Integer stalenessInMinutes = 60 * 24; // Once every day

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getStalenessInMinutes() {
        return stalenessInMinutes;
    }

    public void setStalenessInMinutes(Integer stalenessInMinutes) {
        this.stalenessInMinutes = stalenessInMinutes;
    }
}
