package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;
import se.tink.backend.aggregation.configuration.CircuitBreakerConfiguration;

public class AggregationWorkerConfiguration {
    public static final String DEFAULT_DEBUG_LOG_DIR = "log";

    /**
     * The place where custom logs are stored. Defaults to "log" relative to $CWD.
     */
    @JsonProperty
    private String debugLogDir = DEFAULT_DEBUG_LOG_DIR;
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
