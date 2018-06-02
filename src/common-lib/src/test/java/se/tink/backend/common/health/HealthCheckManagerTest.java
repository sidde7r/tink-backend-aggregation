package se.tink.backend.common.health;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableMap;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.common.health.HealthCheckManager.Outcome;
import se.tink.backend.utils.LogUtils;

public class HealthCheckManagerTest {

    private RuntimeException myException;
    private Outcome healthyWithoutMessage;
    private Outcome healthyWithMessage;
    private Outcome unhealthyWithoutMessage;
    private Outcome unhealthyWithThrowable;
    private long twoSecondsInNanos;

    @Before
    public void setUp() {
        myException = new RuntimeException("My Error");
        twoSecondsInNanos = TimeUnit.NANOSECONDS.convert(2, TimeUnit.SECONDS);
        healthyWithoutMessage = new HealthCheckManager.Outcome("healthyWithoutMessage", HealthCheck.Result.healthy(),
                twoSecondsInNanos);
        healthyWithMessage = new HealthCheckManager.Outcome("healthyWithMessage",
                HealthCheck.Result.healthy("My message"), twoSecondsInNanos);
        unhealthyWithoutMessage = new HealthCheckManager.Outcome("unhealthyWithoutMessage",
                HealthCheck.Result.unhealthy("My message"), twoSecondsInNanos);
        unhealthyWithThrowable = new HealthCheckManager.Outcome("unhealthyWithThrowable",
                HealthCheck.Result.unhealthy(myException), twoSecondsInNanos);
    }
    
    @Test
    public void testOutcomeLogFormat() {
        Assert.assertEquals("Healthy `healthyWithoutMessage`. Took 2000 ms.",
                healthyWithoutMessage.constructLogMessage());
        Assert.assertEquals("Healthy `healthyWithMessage`: My message. Took 2000 ms.",
                healthyWithMessage.constructLogMessage());
        Assert.assertEquals("Unhealthy `unhealthyWithoutMessage`: My message. Took 2000 ms.",
                unhealthyWithoutMessage.constructLogMessage());
        Assert.assertEquals("Unhealthy `unhealthyWithThrowable`: My Error. Took 2000 ms.",
                unhealthyWithThrowable.constructLogMessage());
    }
    
    @Test
    public void testLogLevels() {
        // No need to test the exact log message here. It's tested in #testOutcomeLogFormat.

        verifyOnlyLoggedTo(healthyWithoutMessage).debug(healthyWithoutMessage.constructLogMessage());
        verifyOnlyLoggedTo(healthyWithMessage).debug(healthyWithMessage.constructLogMessage());
        verifyOnlyLoggedTo(unhealthyWithoutMessage).error(unhealthyWithoutMessage.constructLogMessage());
        verifyOnlyLoggedTo(unhealthyWithThrowable).error(unhealthyWithThrowable.constructLogMessage(),
                unhealthyWithThrowable.result.getError());
    }

    private LogUtils verifyOnlyLoggedTo(Outcome outcome) {
        LogUtils log = Mockito.mock(LogUtils.class);
        outcome.logTo(log);
        return Mockito.verify(log, Mockito.only());
    }

    private class HealthyCheck extends HealthCheck {

        public boolean called = false;

        @Override
        protected Result check() throws Exception {
            called = true;
            return Result.healthy();
        }
        
    }
    
    private class UnhealthyCheck extends HealthCheck {

        public boolean called = false;

        @Override
        protected Result check() throws Exception {
            called = true;
            return Result.unhealthy("Something bad happened");
        }
        
    }
    
    private class ThrowingCheck extends HealthCheck {

        public boolean called = false;

        @Override
        protected Result check() throws Exception {
            called = true;
            throw new RuntimeException();
        }
        
    }
    
    @Test
    public void testHealthManagerCheckSuccess() {
        // Given
        LogUtils log = Mockito.mock(LogUtils.class);
        HealthyCheck check = new HealthyCheck();
        HealthCheckManager manager = new HealthCheckManager(log, ImmutableMap.<String, HealthCheck> of("testCheck",
                check));

        // When
        boolean outcome = manager.check();

        // Then
        Assert.assertTrue(check.called);
        Assert.assertTrue(outcome);
    }

    @Test
    public void testHealthManagerCheckFailure() {
        // Given
        LogUtils log = Mockito.mock(LogUtils.class);
        UnhealthyCheck check = new UnhealthyCheck();
        HealthCheckManager manager = new HealthCheckManager(log, ImmutableMap.<String, HealthCheck> of("testCheck",
                check));

        // When
        boolean outcome = manager.check();

        // Then
        Assert.assertTrue(check.called);
        Assert.assertFalse(outcome);
    }

    @Test
    public void testHealthManagerCheckException() {
        // Given
        LogUtils log = Mockito.mock(LogUtils.class);
        ThrowingCheck check = new ThrowingCheck();
        HealthCheckManager manager = new HealthCheckManager(log, ImmutableMap.<String, HealthCheck> of("testCheck",
                check));

        // When
        boolean outcome = manager.check();

        // Then
        Assert.assertTrue(check.called);
        Assert.assertFalse(outcome);
    }

}
