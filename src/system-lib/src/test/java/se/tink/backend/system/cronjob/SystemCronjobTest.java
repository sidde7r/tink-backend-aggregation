package se.tink.backend.system.cronjob;

import se.tink.libraries.metrics.MetricRegistry;
import java.util.Optional;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class SystemCronjobTest {
    private static class OverlappingTestJob extends SystemCronJob implements Runnable {

        private AtomicInteger executions;
        private Optional<CountDownLatch> startedLatch;
        private Optional<CountDownLatch> allowedToFinishLatch;
        
        public OverlappingTestJob(AtomicInteger executions) {
            this(executions, Optional.empty(), Optional.empty());
        }

        public OverlappingTestJob(AtomicInteger executions, CountDownLatch startedLatch, CountDownLatch allowedToFinishLatch) {
            this(executions, Optional.of(startedLatch), Optional.of(allowedToFinishLatch));
        }

        private OverlappingTestJob(AtomicInteger executions, Optional<CountDownLatch> startedLatch,
                Optional<CountDownLatch> allowedToFinishLatch) {
            super(OverlappingTestJob.class, new AtomicBoolean(true));
            this.executions = executions;
            this.startedLatch = startedLatch;
            this.allowedToFinishLatch = allowedToFinishLatch;
        }

        @Override
        public void executeIsolated(JobExecutionContext context) {
            if (startedLatch.isPresent()) {
                startedLatch.get().countDown();
            }
            if (allowedToFinishLatch.isPresent()) {
                Uninterruptibles.awaitUninterruptibly(allowedToFinishLatch.get());
            }
            executions.addAndGet(1);
        }

        @Override
        public void run() {
            try {
                execute(null);
            } catch (JobExecutionException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Test
    public void testOverlappingJobsAreNeverHappening() throws InterruptedException, BrokenBarrierException {
        final AtomicInteger executions = new AtomicInteger(0);

        MetricRegistry registry = new MetricRegistry();
        OverlappingTestJob.setMetricRegistry(registry);

        CountDownLatch thread1Started = new CountDownLatch(1);
        CountDownLatch thread1AllowedToFinish = new CountDownLatch(1);
        // Important to use different OverlappingTestJob instances. That's what Quartz does.
        Thread thread1 = new Thread(new OverlappingTestJob(executions, thread1Started, thread1AllowedToFinish));
        thread1.start();
        Assert.assertTrue(thread1Started.await(1, TimeUnit.MINUTES));

        Thread thread2 = new Thread(new OverlappingTestJob(executions));
        thread2.start();


        long tenSecondsInMillis = TimeUnit.MINUTES.toSeconds(1);
        thread2.join(tenSecondsInMillis);

        thread1AllowedToFinish.countDown();
        thread1.join(tenSecondsInMillis);
        
        Assert.assertEquals(1, executions.get());
    }
}
