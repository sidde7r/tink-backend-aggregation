package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RateThisAppConfiguration {

    @JsonProperty
    private boolean enabled = false;
    @JsonProperty
    private int minDaysSinceCreated = 30;
    @JsonProperty
    private int maxDaysSinceCreated = 120;
    @JsonProperty
    private int maxInitialCategorization = 90;
    @JsonProperty
    private int minCategorizationLevel = 95;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMinDaysSinceCreated() {
        return minDaysSinceCreated;
    }

    public int getMaxDaysSinceCreated() {
        return maxDaysSinceCreated;
    }

    public int getMaxInitialCategorization() {
        return maxInitialCategorization;
    }

    public int getMinCategorizationLevel() {
        return minCategorizationLevel;
    }

}
