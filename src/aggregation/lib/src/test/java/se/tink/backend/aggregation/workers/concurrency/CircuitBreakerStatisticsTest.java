package se.tink.backend.aggregation.workers.concurrency;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import se.tink.backend.aggregation.configuration.CircuitBreakerConfiguration;
import se.tink.libraries.concurrency.FakeTicker;
import se.tink.libraries.metrics.MetricRegistry;

public class CircuitBreakerStatisticsTest {

    @Test(expected = IllegalArgumentException.class)
    public void testNonePositiveTimeLimitValidation() {
        new CircuitBreakerStatistics(
                0,
                TimeUnit.HOURS,
                singletonList(1),
                0.5,
                1,
                1,
                new MetricRegistry(),
                "providerName",
                "className",
                "market");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNoneEmptyListOfMultiplicationFactorsValidation() {
        new CircuitBreakerStatistics(
                1,
                TimeUnit.HOURS,
                emptyList(),
                0.5,
                1,
                1,
                new MetricRegistry(),
                "providerName",
                "className",
                "market");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonePositiveErrorRatioThresholdValidation() {
        new CircuitBreakerStatistics(
                1,
                TimeUnit.HOURS,
                singletonList(1),
                0.0,
                1,
                1,
                new MetricRegistry(),
                "providerName",
                "className",
                "market");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonePositiveCircuitBreakerThresholdValidation() {
        new CircuitBreakerStatistics(
                1,
                TimeUnit.HOURS,
                singletonList(1),
                0.5,
                0,
                1,
                new MetricRegistry(),
                "providerName",
                "className",
                "market");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNonePositiveCircuitBreakBreakerThresholdValidation() {
        new CircuitBreakerStatistics(
                1,
                TimeUnit.HOURS,
                singletonList(1),
                0.5,
                1,
                0,
                new MetricRegistry(),
                "providerName",
                "className",
                "market");
    }

    @Test
    public void testNotCircuitBrokenAndMultiplicationFactorOfOneAfterInstantiation() {
        final FakeTicker ticker = new FakeTicker();
        CircuitBreakerStatistics statistics =
                new CircuitBreakerStatistics(
                        1,
                        TimeUnit.SECONDS,
                        ImmutableList.of(1, 2),
                        0.5,
                        2,
                        5,
                        new MetricRegistry(),
                        "providerName",
                        "className",
                        "market",
                        ticker);

        assertFalse(statistics.getStatus().isCircuitBroken());
        assertEquals(statistics.getStatus().getRateLimitMultiplicationFactor(), 1);
    }

    @Test
    public void testBasicFunctionalityWithLevelingAndResetting() {
        final FakeTicker ticker = new FakeTicker();
        final CircuitBreakerConfiguration circuitBreakerConfiguration =
                new CircuitBreakerConfiguration();
        CircuitBreakerStatistics statistics =
                new CircuitBreakerStatistics(
                        1,
                        TimeUnit.SECONDS,
                        circuitBreakerConfiguration.getRateLimitMultiplicationFactors(),
                        0.5,
                        2,
                        5,
                        new MetricRegistry(),
                        "providerName",
                        "className",
                        "market",
                        ticker);

        statistics.registerError();
        statistics.registerError();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 1);
        assertFalse(statistics.getStatus().isCircuitBroken());

        statistics.registerError();
        statistics.registerError();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 0);
        assertTrue(statistics.getStatus().isCircuitBroken());
        assertEquals(
                statistics.getStatus().getRateLimitMultiplicationFactor(),
                circuitBreakerConfiguration.getRateLimitMultiplicationFactors().get(0).intValue());

        statistics.registerSuccess();
        statistics.registerSuccess();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 1);
        assertTrue(statistics.getStatus().isCircuitBroken());
        assertEquals(
                statistics.getStatus().getRateLimitMultiplicationFactor(),
                circuitBreakerConfiguration.getRateLimitMultiplicationFactors().get(1).intValue());

        statistics.registerSuccess();
        statistics.registerSuccess();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 2);
        assertTrue(statistics.getStatus().isCircuitBroken());
        assertEquals(
                statistics.getStatus().getRateLimitMultiplicationFactor(),
                circuitBreakerConfiguration.getRateLimitMultiplicationFactors().get(2).intValue());

        statistics.registerSuccess();
        statistics.registerSuccess();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 3);
        assertTrue(statistics.getStatus().isCircuitBroken());
        assertEquals(
                statistics.getStatus().getRateLimitMultiplicationFactor(),
                circuitBreakerConfiguration.getRateLimitMultiplicationFactors().get(3).intValue());

        statistics.registerSuccess();
        statistics.registerSuccess();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 4);
        assertTrue(statistics.getStatus().isCircuitBroken());
        assertEquals(
                statistics.getStatus().getRateLimitMultiplicationFactor(),
                circuitBreakerConfiguration.getRateLimitMultiplicationFactors().get(4).intValue());

        statistics.registerError();
        statistics.registerError();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 0);
        assertTrue(statistics.getStatus().isCircuitBroken());
        assertEquals(
                statistics.getStatus().getRateLimitMultiplicationFactor(),
                circuitBreakerConfiguration.getRateLimitMultiplicationFactors().get(0).intValue());
    }

    @Test
    public void testBasicFunctionalityWithLevelingAndEscapeOfCircuitBreak() {
        final FakeTicker ticker = new FakeTicker();
        final CircuitBreakerConfiguration circuitBreakerConfiguration =
                new CircuitBreakerConfiguration();
        CircuitBreakerStatistics statistics =
                new CircuitBreakerStatistics(
                        1,
                        TimeUnit.SECONDS,
                        circuitBreakerConfiguration.getRateLimitMultiplicationFactors(),
                        0.5,
                        2,
                        5,
                        new MetricRegistry(),
                        "providerName",
                        "className",
                        "market",
                        ticker);

        statistics.registerError();
        statistics.registerError();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 1);
        assertFalse(statistics.getStatus().isCircuitBroken());

        statistics.registerError();
        statistics.registerError();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 0);
        assertTrue(statistics.getStatus().isCircuitBroken());
        assertEquals(
                statistics.getStatus().getRateLimitMultiplicationFactor(),
                circuitBreakerConfiguration.getRateLimitMultiplicationFactors().get(0).intValue());

        statistics.registerSuccess();
        statistics.registerSuccess();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 1);
        assertTrue(statistics.getStatus().isCircuitBroken());
        assertEquals(
                statistics.getStatus().getRateLimitMultiplicationFactor(),
                circuitBreakerConfiguration.getRateLimitMultiplicationFactors().get(1).intValue());

        statistics.registerSuccess();
        statistics.registerSuccess();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 2);
        assertTrue(statistics.getStatus().isCircuitBroken());
        assertEquals(
                statistics.getStatus().getRateLimitMultiplicationFactor(),
                circuitBreakerConfiguration.getRateLimitMultiplicationFactors().get(2).intValue());

        statistics.registerSuccess();
        statistics.registerSuccess();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 3);
        assertTrue(statistics.getStatus().isCircuitBroken());
        assertEquals(
                statistics.getStatus().getRateLimitMultiplicationFactor(),
                circuitBreakerConfiguration.getRateLimitMultiplicationFactors().get(3).intValue());

        statistics.registerSuccess();
        statistics.registerSuccess();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 4);
        assertTrue(statistics.getStatus().isCircuitBroken());
        assertEquals(
                statistics.getStatus().getRateLimitMultiplicationFactor(),
                circuitBreakerConfiguration.getRateLimitMultiplicationFactors().get(4).intValue());

        statistics.registerSuccess();
        statistics.registerSuccess();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 0);
        assertFalse(statistics.getStatus().isCircuitBroken());
    }

    @Test
    public void testNoSubsequentIfNoInput() {
        final FakeTicker ticker = new FakeTicker();
        CircuitBreakerStatistics statistics =
                new CircuitBreakerStatistics(
                        1,
                        TimeUnit.SECONDS,
                        ImmutableList.of(1, 2),
                        0.5,
                        2,
                        5,
                        new MetricRegistry(),
                        "providerName",
                        "className",
                        "market",
                        ticker);

        statistics.registerError();

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 1);

        ticker.now.addAndGet(TimeUnit.MILLISECONDS.toNanos(1500));

        assertEquals(statistics.getStatus().getConsecutiveOperationsCounter(), 1);
    }
}
