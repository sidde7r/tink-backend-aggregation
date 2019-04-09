package se.tink.libraries.concurrency;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import se.tink.libraries.executor.ExecutorServiceUtils;

public class ListenableThreadPoolSubmitterTest {

    private final ThreadFactory THREAD_FACTORY = new ThreadFactoryBuilder().build();

    @Test
    public void testGeneralUseCase() throws Exception {
        BlockingQueue<WrappedCallableListenableFutureTask<Callable<?>, ?>> executorServiceQueue =
                Queues.newLinkedBlockingQueue();
        ListenableThreadPoolSubmitter<Callable<?>> executorService =
                ListenableThreadPoolSubmitter.builder(
                                executorServiceQueue, new TypedThreadPoolBuilder(1, THREAD_FACTORY))
                        .build();
        try {

            final CountDownLatch started = new CountDownLatch(1);
            final CountDownLatch allowedToFinish = new CountDownLatch(1);

            final AtomicInteger runs = new AtomicInteger(0);
            ListenableFuture<?> firstFuture =
                    executorService.submit(
                            (Callable<Integer>)
                                    () -> {
                                        started.countDown();
                                        int result = runs.incrementAndGet();
                                        try {
                                            Assert.assertTrue(
                                                    allowedToFinish.await(1, TimeUnit.MINUTES));
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        return result;
                                    });

            Callable<Integer> incrementor = runs::incrementAndGet;

            Assert.assertTrue(started.await(1, TimeUnit.MINUTES));
            executorService.submit(incrementor);
            ListenableFuture<?> lastFuture = executorService.submit(incrementor);

            Assert.assertFalse(firstFuture.isDone());

            // Queue could either be 1 or 2 because the QueuePopper could have taken one item
            // waiting to put it on the
            // thread pool.
            Assert.assertTrue(ImmutableSet.of(1, 2).contains(executorServiceQueue.size()));

            allowedToFinish.countDown();

            lastFuture.get();
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

    @Test
    public void testErrorInRunnable() throws Exception {
        ThreadFactory threadFactory =
                new ThreadFactoryBuilder().setNameFormat("kafka-receiver-%d").build();
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> blockingQueue =
                Queues.newArrayBlockingQueue(10);
        ListenableThreadPoolExecutor<Runnable> workerExecutor =
                ListenableThreadPoolExecutor.builder(
                                blockingQueue, new TypedThreadPoolBuilder(10, threadFactory))
                        .withRejectedHandler(new TypeSafeBlockingExecutionHandler<>())
                        .build();

        final ArrayList<ListenableFuture> futures = Lists.newArrayList();

        final Random random = new Random();
        for (int i = 0; i < 100000; i++) {
            final ListenableFuture future =
                    workerExecutor.execute(
                            () -> {
                                if (random.nextInt(1) > 0) {
                                    throw new RuntimeException("Expected.");
                                }
                            });
            futures.add(future);
        }

        for (ListenableFuture future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                // Ok.
            }
        }
    }

    @Test
    public void testErrorInCallback() throws Exception {
        ThreadFactory threadFactory =
                new ThreadFactoryBuilder().setNameFormat("kafka-receiver-%d").build();
        BlockingQueue<WrappedRunnableListenableFutureTask<Runnable, ?>> blockingQueue =
                Queues.newArrayBlockingQueue(10);
        ListenableThreadPoolExecutor<Runnable> workerExecutor =
                ListenableThreadPoolExecutor.builder(
                                blockingQueue, new TypedThreadPoolBuilder(10, threadFactory))
                        .withRejectedHandler(new TypeSafeBlockingExecutionHandler<>())
                        .build();

        final ArrayList<ListenableFuture> futures = Lists.newArrayList();

        final Random random = new Random();
        for (int i = 0; i < 100000; i++) {
            final ListenableFuture future =
                    workerExecutor.execute(
                            () -> {
                                // Expected empty.
                            });
            future.addListener(
                    () -> {
                        if (random.nextInt(1) > 0) {
                            throw new RuntimeException("Expected.");
                        }
                    },
                    MoreExecutors.directExecutor());
            futures.add(future);
        }

        for (ListenableFuture future : futures) {
            try {
                future.get();
            } catch (ExecutionException e) {
                // Ok.
            }
        }
    }
}
