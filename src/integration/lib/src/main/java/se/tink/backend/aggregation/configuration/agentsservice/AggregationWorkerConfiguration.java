package se.tink.backend.aggregation.configuration.agentsservice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AggregationWorkerConfiguration {

    public static final String DEFAULT_DEBUG_LOG_DIR = "log";

    /** The place where custom logs are stored. Defaults to "log" relative to $CWD. */
    @JsonProperty private String debugLogDir = DEFAULT_DEBUG_LOG_DIR;

    @JsonProperty private int debugLogFrequencyPercent;

    @JsonProperty private String longTermStorageDisputeBasePrefix;

    @JsonProperty
    private CircuitBreakerConfiguration circuitBreaker = new CircuitBreakerConfiguration();

    public void setDebugLogFrequencyPercent(int debugLogFrequencyPercent) {
        Preconditions.checkArgument(
                debugLogFrequencyPercent >= 0 && debugLogFrequencyPercent <= 100,
                "Debug log frequency has to be between 0 and 100.");
        this.debugLogFrequencyPercent = debugLogFrequencyPercent;
    }
}
