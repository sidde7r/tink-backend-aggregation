package se.tink.backend.aggregation.workers.concurrency;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Ticker;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.gauges.LastUpdateGauge;

public class CircuitBreakerStatistics {
    private final LastUpdateGauge circuitBrokenGauge;
    private AtomicInteger errorCounter = new AtomicInteger();
    private AtomicInteger successCounter = new AtomicInteger();
    private AtomicInteger consecutiveOperationsCounter = new AtomicInteger();
    private AtomicBoolean circuitBroken = new AtomicBoolean();

    private static final MetricId CIRCUIT_BROKEN_PROVIDERS =
            MetricId.newId("circuit_broken_providers");

    private final long minNanosForReset;
    private final Ticker ticker;
    private long lastTick;
    private final double threshold;
    private final int circuitBreakerThreshold;
    private final int breakCircuitBreakerThreshold;
    private final List<Integer> rateLimitMultiplicationFactors;

    public CircuitBreakerStatistics(
            int timeLimit,
            TimeUnit timeLimitUnit,
            List<Integer> rateLimitMultiplicationFactors,
            double errorRatioThreshold,
            int circuitBreakerThreshold,
            int breakCircuitBreakerThreshold,
            MetricRegistry metricRegistry,
            String providerName,
            String className,
            String market) {
        this(
                timeLimit,
                timeLimitUnit,
                rateLimitMultiplicationFactors,
                errorRatioThreshold,
                circuitBreakerThreshold,
                breakCircuitBreakerThreshold,
                metricRegistry,
                providerName,
                className,
                market,
                Ticker.systemTicker());
    }

    CircuitBreakerStatistics(
            int timeLimit,
            TimeUnit timeLimitUnit,
            List<Integer> rateLimitMultiplicationFactors,
            double errorRatioThreshold,
            int circuitBreakerThreshold,
            int breakCircuitBreakerThreshold,
            MetricRegistry metricRegistry,
            String providerName,
            String className,
            String market,
            Ticker ticker) {
        Preconditions.checkArgument(timeLimit > 0);
        Preconditions.checkArgument(!rateLimitMultiplicationFactors.isEmpty());
        Preconditions.checkArgument(errorRatioThreshold > 0);
        Preconditions.checkArgument(circuitBreakerThreshold > 0);
        Preconditions.checkArgument(breakCircuitBreakerThreshold > 0);
        Preconditions.checkArgument(metricRegistry != null);
        Preconditions.checkArgument(!Strings.isNullOrEmpty(providerName));

        this.minNanosForReset = timeLimitUnit.toNanos(timeLimit);
        this.rateLimitMultiplicationFactors = rateLimitMultiplicationFactors;
        this.ticker = ticker;
        this.lastTick = ticker.read();
        this.threshold = errorRatioThreshold;
        this.circuitBreakerThreshold = circuitBreakerThreshold;
        this.breakCircuitBreakerThreshold = breakCircuitBreakerThreshold;

        this.circuitBrokenGauge =
                metricRegistry.lastUpdateGauge(
                        CIRCUIT_BROKEN_PROVIDERS
                                .label("provider", providerName)
                                .label("className", className)
                                .label("market", market));
    }

    private boolean needToReset() {
        long now = ticker.read();
        return (now - lastTick) > minNanosForReset;
    }

    private void updateStatusAndResetCountersIfNecessary() {
        if (!needToReset()) {
            return;
        }

        if (getTotalOfErrorAndSuccessCounters() > 0) {
            // Update status
            updateCircuitBreakerStatus();
        }

        // Reset counters
        errorCounter.set(0);
        successCounter.set(0);

        lastTick = ticker.read();
    }

    private void updateCircuitBreakerStatus() {
        countConsecutiveOperations();
        toggleCircuitBreaker();
    }

    private int getTotalOfErrorAndSuccessCounters() {
        return successCounter.get() + errorCounter.get();
    }

    private void countConsecutiveOperations() {
        if (errorCounter.doubleValue() / getTotalOfErrorAndSuccessCounters() > threshold) {
            if (circuitBroken.get()) {
                consecutiveOperationsCounter.set(0);
            } else {
                consecutiveOperationsCounter.incrementAndGet();
            }
        } else {
            if (circuitBroken.get()) {
                consecutiveOperationsCounter.incrementAndGet();
            } else {
                consecutiveOperationsCounter.set(0);
            }
        }
    }

    private void toggleCircuitBreaker() {
        if (!circuitBroken.get() && consecutiveOperationsCounter.get() >= circuitBreakerThreshold) {
            consecutiveOperationsCounter.set(0);
            circuitBroken.set(true);
        } else if (circuitBroken.get()
                && consecutiveOperationsCounter.get() >= breakCircuitBreakerThreshold) {
            consecutiveOperationsCounter.set(0);
            circuitBroken.set(false);
        }

        circuitBrokenGauge.update(circuitBroken.get() ? 1 : 0);
    }

    public void registerError() {
        updateStatusAndResetCountersIfNecessary();
        errorCounter.incrementAndGet();
    }

    public void registerSuccess() {
        updateStatusAndResetCountersIfNecessary();
        successCounter.incrementAndGet();
    }

    public static class CircuitBreakerStatus {
        private boolean circuitBroken;
        private int rateLimitMultiplicationFactor;
        private int consecutiveOperationsCounter;

        public boolean isCircuitBroken() {
            return circuitBroken;
        }

        public int getRateLimitMultiplicationFactor() {
            return rateLimitMultiplicationFactor;
        }

        public int getConsecutiveOperationsCounter() {
            return consecutiveOperationsCounter;
        }
    }

    public CircuitBreakerStatus getStatus() {
        updateStatusAndResetCountersIfNecessary();

        CircuitBreakerStatus status = new CircuitBreakerStatus();
        status.circuitBroken = circuitBroken.get();
        status.consecutiveOperationsCounter = consecutiveOperationsCounter.get();
        // To assure that we don't go out of list range use modulus list size when getting from the
        // list
        status.rateLimitMultiplicationFactor =
                rateLimitMultiplicationFactors.get(
                        consecutiveOperationsCounter.get() % rateLimitMultiplicationFactors.size());
        return status;
    }
}
