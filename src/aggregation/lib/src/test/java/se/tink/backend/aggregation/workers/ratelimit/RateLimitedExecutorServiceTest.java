package se.tink.backend.aggregation.workers.ratelimit;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.CountDownLatch;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Provider;
import se.tink.libraries.concurrency.ListenableThreadPoolExecutor;
import se.tink.libraries.concurrency.NamedRunnable;
import se.tink.libraries.concurrency.TypedThreadPoolBuilder;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.provider.ProviderDto;

public class RateLimitedExecutorServiceTest {

    private static final Logger logger =
            LoggerFactory.getLogger(RateLimitedExecutorServiceTest.class);
    public static final MetricRegistry dummyMetricRegistry = new MetricRegistry();
    private static final int MAX_QUEUED_UP = 200;

    private static class TestRunnable implements Runnable {
        private String identifier;
        private final Runnable callback;

        public TestRunnable(String identifier, Runnable callback) {
            this.identifier = identifier;
            this.callback = callback;
        }

        @Override
        public void run() {
            logger.info(this.toString());
            callback.run();
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("identifier", identifier).toString();
        }
    }

    public Provider getProvider() {
        Provider dummyProvider = new Provider();
        dummyProvider.setName("test-market-a");
        dummyProvider.setClassName("ThisIsNotAClass");
        dummyProvider.setType(ProviderDto.ProviderTypes.FIRST_PARTY);
        return dummyProvider;
    }

    @Test
    public void testOnlyOneRateLimiterIsCreatedPerProviderName() throws Exception {

        ListenableThreadPoolExecutor<Runnable> delegateExecutor =
                ListenableThreadPoolExecutor.builder(
                                Queues.newLinkedBlockingQueue(),
                                new TypedThreadPoolBuilder(1, new ThreadFactoryBuilder().build()))
                        .build();

        RateLimitedExecutorService executorService =
                new RateLimitedExecutorService(
                        false, delegateExecutor, dummyMetricRegistry, MAX_QUEUED_UP);
        executorService.start();

        CountDownLatch countDownLatch = new CountDownLatch(2);
        NamedRunnable runnable =
                new NamedRunnable(new TestRunnable("a", countDownLatch::countDown), "a");

        executorService.execute(runnable, getProvider());
        executorService.execute(runnable, getProvider());

        countDownLatch.await();

        Assert.assertEquals(1, executorService.getCacheSize());
        executorService.stop();
    }
}
