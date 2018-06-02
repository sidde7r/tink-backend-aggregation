package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class AggregationWorkerConfiguration {
    /**
     * The place where custom logs are stored. Defaults to "log" relative to $CWD.
     */
    @NotEmpty
    @JsonProperty
    private String debugLogDir = "log";
    @JsonProperty
    private String collectorSubscriptionKey;
    @JsonProperty
    private CircuitBreakerConfiguration circuitBreaker = new CircuitBreakerConfiguration();

    public String getDebugLogDir() {
        return debugLogDir;
    }

    public String getCollectorSubscriptionKey() {
        return collectorSubscriptionKey;
    }

    public CircuitBreakerConfiguration getCircuitBreaker() {
        return circuitBreaker;
    }
}
