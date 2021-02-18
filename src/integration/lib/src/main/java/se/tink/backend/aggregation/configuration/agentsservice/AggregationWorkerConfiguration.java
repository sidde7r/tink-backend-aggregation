package se.tink.backend.aggregation.configuration.agentsservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;

public class AggregationWorkerConfiguration {
    public static final String DEFAULT_DEBUG_LOG_DIR = "log";

    /** The place where custom logs are stored. Defaults to "log" relative to $CWD. */
    @JsonProperty private String debugLogDir = DEFAULT_DEBUG_LOG_DIR;

    @JsonProperty private int debugLogFrequencyPercent;

    @JsonProperty private String longTermStorageDisputeBasePrefix;

    @JsonProperty
    private CircuitBreakerConfiguration circuitBreaker = new CircuitBreakerConfiguration();

    public void setDefaultDebugLogFrequency(int debugLogFrequencyPercent) {
        Preconditions.checkArgument(
                debugLogFrequencyPercent >= 0 && debugLogFrequencyPercent <= 100,
                "Debug log frequency has to be between 0 and 100.");
        this.debugLogFrequencyPercent = debugLogFrequencyPercent;
    }

    public String getDebugLogDir() {
        return debugLogDir;
    }

    public CircuitBreakerConfiguration getCircuitBreaker() {
        return circuitBreaker;
    }

    public int getDebugFrequencyPercent() {
        return debugLogFrequencyPercent;
    }

    public void setLongTermStorageDisputeBasePrefix(String longTermStorageDisputeBasePrefix) {
        this.longTermStorageDisputeBasePrefix = longTermStorageDisputeBasePrefix;
    }

    public String getLongTermStorageDisputeBasePrefix() {
        return longTermStorageDisputeBasePrefix;
    }
}
