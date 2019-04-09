package se.tink.libraries.concurrency;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.executor.ExecutorServiceUtils;
import se.tink.libraries.metrics.MetricRegistry;

public class ListenableThreadPoolExecutorTest {
    private final ThreadFactory threadFactory = new ThreadFactoryBuilder().build();

    @Test
    public void testGeneralUseCase() throws Exception {
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> executorServiceQueue =
                Queues.newLinkedBlockingQueue();
        final MetricRegistry registry = new MetricRegistry();

        ListenableThreadPoolExecutor<Runnable> executorService =
                ListenableThreadPoolExecutor.builder(
                                executorServiceQueue, new TypedThreadPoolBuilder(1, threadFactory))
                        .withMetric(registry, "test_executor")
                        .build();

        try {

            final CountDownLatch started = new CountDownLatch(1);
            final CountDownLatch allowedToFinish = new CountDownLatch(1);

            final AtomicInteger runs = new AtomicInteger(0);
            ListenableFuture<?> firstFuture =
                    executorService.execute(
                            () -> {
                                started.countDown();
                                runs.incrementAndGet();
                                try {
                                    Assert.assertTrue(allowedToFinish.await(1, TimeUnit.MINUTES));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            });

            Runnable incrementor = runs::incrementAndGet;

            Assert.assertTrue(started.await(1, TimeUnit.MINUTES));
            ListenableFuture<?> middleFuture = executorService.execute(incrementor);
            ListenableFuture<?> lastFuture = executorService.execute(incrementor);

            Assert.assertFalse(firstFuture.isDone());

            // Queue could either be 1 or 2 because the QueuePopper could have taken one item
            // waiting to put it on the
            // thread pool.
            Assert.assertTrue(ImmutableSet.of(1, 2).contains(executorServiceQueue.size()));

            allowedToFinish.countDown();

            lastFuture.get();
            middleFuture.get();
            Assert.assertEquals(0, executorServiceQueue.size());
            Assert.assertEquals(3, runs.get());

            // Can't be asserted because of some race condition. according to getActiveCount's
            // JavaDoc the returned
            // value is only approximate which could explain things.
            // Assert.assertEquals(0, executorService.getThreadPool().getActiveCount());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Assert.assertTrue(
                    ExecutorServiceUtils.shutdownExecutor(
                            "identifier", executorService, 10, TimeUnit.SECONDS));
        }
    }
}
