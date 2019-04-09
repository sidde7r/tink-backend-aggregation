package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CircuitBreakerConfiguration {

    @JsonProperty private double rateLimitRate = 1d / 60; // 1 action per minute

    @JsonProperty
    private List<Integer> rateLimitMultiplicationFactors = ImmutableList.of(1, 2, 4, 8, 16);

    @JsonProperty private int resetInterval = 60;
    @JsonProperty private String resetIntervalTimeUnit = "SECONDS";
    @JsonProperty private double failRatioThreshold = 0.5;
    @JsonProperty private CircuitBreakerMode mode = CircuitBreakerMode.ENABLED;
    @JsonProperty private int circuitBreakerThreshold = 2;
    @JsonProperty private int breakCircuitBreakerThreshold = 5;

    public double getRateLimitRate() {
        return rateLimitRate;
    }

    public List<Integer> getRateLimitMultiplicationFactors() {
        return rateLimitMultiplicationFactors;
    }

    public int getResetInterval() {
        return resetInterval;
    }

    public TimeUnit getResetIntervalTimeUnit() {
        return TimeUnit.valueOf(resetIntervalTimeUnit);
    }

    public double getFailRatioThreshold() {
        return failRatioThreshold;
    }

    public CircuitBreakerMode getMode() {
        return mode;
    }

    public int getCircuitBreakerThreshold() {
        return circuitBreakerThreshold;
    }

    public int getBreakCircuitBreakerThreshold() {
        return breakCircuitBreakerThreshold;
    }
}
