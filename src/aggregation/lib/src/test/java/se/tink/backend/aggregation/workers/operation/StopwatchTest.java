package se.tink.backend.aggregation.workers.operation;

import com.google.common.base.Stopwatch;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Test;

public class StopwatchTest {

    @Test
    public void stopWatchShouldBeStoppedAndResumed() throws Exception {
        long sleepTime = 500;
        Stopwatch stopwatch = Stopwatch.createStarted();
        stopwatch.stop();
        Thread.sleep(sleepTime);
        stopwatch.start();
        stopwatch.stop();
        long elapsedTime = stopwatch.elapsed(TimeUnit.MILLISECONDS);
        // Elapsed time should be 0 or very close to 0, to be on the safe side we are more tolerant
        // here
        Assert.assertTrue(elapsedTime < (sleepTime / 2));
    }
}
