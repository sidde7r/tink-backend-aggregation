package se.tink.backend.aggregation.agents.abnamro.ics.retry;

import com.github.rholder.retry.RetryException;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.log.legacy.LogUtils;

public class RetryerBuilderTest {

    private static final LogUtils log = new LogUtils(RetryerBuilderTest.class);

    @Test
    public void testNullResult() throws ExecutionException, RetryException {
        Retryer<String> retryer = RetryerBuilder
                .<String>newBuilder(log, "testing retry")
                .withStopStrategy(StopStrategies.stopAfterDelay(10, TimeUnit.SECONDS))
                .withWaitStrategy(WaitStrategies.fibonacciWait())
                .retryIfException().build();
        
        String result = retryer.call(() -> null);
        Assert.assertNull(result);
    }

}
