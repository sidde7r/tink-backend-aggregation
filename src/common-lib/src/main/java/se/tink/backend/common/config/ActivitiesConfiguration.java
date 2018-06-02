package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActivitiesConfiguration {

    @JsonProperty
    private RateThisAppConfiguration rateThisApp = new RateThisAppConfiguration();
    @JsonProperty
    private boolean shouldGroupActivities = true;
    @JsonProperty
    private boolean shouldFilterActivities = true;
    @JsonProperty
    private boolean shouldGenerateHighBalanceWarning = true;
    @JsonProperty
    private boolean shouldGenerateLowBalanceWarning = true;
    @JsonProperty
    private boolean shouldGenerateSensitiveMessage = true;

    public RateThisAppConfiguration getRateThisApp() {
        return rateThisApp;
    }

    public boolean shouldGroupActivities() {
        return shouldGroupActivities;
    }

    public boolean shouldFilterActivities() {
        return shouldFilterActivities;
    }

    public boolean shouldGenerateHighBalanceWarning() {
        return shouldGenerateHighBalanceWarning;
    }

    public boolean shouldGenerateLowBalanceWarning() {
        return shouldGenerateLowBalanceWarning;
    }

    public boolean shouldGenerateSensitiveMessage() {
        return shouldGenerateSensitiveMessage;
    }

    public void setShouldGenerateSensitiveMessage(boolean shouldGenerateSensitiveMessage) {
        this.shouldGenerateSensitiveMessage = shouldGenerateSensitiveMessage;
    }
}
