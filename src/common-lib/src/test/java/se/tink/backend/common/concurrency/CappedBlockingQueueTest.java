package se.tink.backend.common.concurrency;

import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.RateLimiter;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class CappedBlockingQueueTest {

    private CappedBlockingQueue<Integer> queue;

    private <E extends Comparable> SortedDelegate<E, E> buildNaturallySortedSetDelegate() {
        return new SortedDelegate<>(Ordering.natural(), Functions.identity());
    }

    private <E> SortedDelegate<E, E> buildArbitrarySortedSetDelegate() {
        final Comparator<E> arbitrary = (a, b) -> Ordering.arbitrary().compare(a, b);
        return new SortedDelegate<>(arbitrary, Functions.identity());
    }

    @Before
    public void setUp() {
        this.queue = CappedBlockingQueue.<Integer>builder().build(1, buildNaturallySortedSetDelegate());
        Assert.assertEquals(0, queue.size());
    }

    private void populateQueue() {
        Assert.assertTrue(queue.offer(42));
    }

    @Test
    public void testPopulateQueue() {
        Assert.assertEquals(0, queue.size());
        populateQueue();
        Assert.assertEquals(1, queue.size());
    }

    @Test
    public void testOffer() {
        // Easiest to test the async operations.
        populateQueue();
        Assert.assertTrue(queue.offer(2));
        Assert.assertTrue(queue.offer(3));
        Assert.assertEquals(1, queue.size());
    }

    @Test
    public void testPollWithoutTimeout() {
        populateQueue();
        Assert.assertTrue(queue.poll() == 42);
        Assert.assertNull(queue.poll());
    }

    @Test
    public void testPeek() {
        populateQueue();
        Assert.assertTrue(queue.peek() == 42);
        Assert.assertEquals(1, queue.size());
        Assert.assertTrue(queue.peek() == 42);
        Assert.assertEquals(1, queue.size());
    }

    @Test
    public void testPollTimeout() throws InterruptedException {
        Assert.assertNull(queue.poll(10, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testOfferTimeout() throws InterruptedException {
        Assert.assertTrue(queue.offer(43, 10, TimeUnit.MILLISECONDS));
        Assert.assertEquals(1, queue.size());
        Assert.assertTrue(queue.offer(43, 10, TimeUnit.MILLISECONDS));
        Assert.assertEquals(1, queue.size());
    }

    @Test
    public void testRemainingCapacity() {
        Assert.assertTrue(queue.remainingCapacity() == 1);
        populateQueue();
        Assert.assertTrue(queue.remainingCapacity() == 0);
    }

    // TODO: test blocking put.
    @Test
    public void testPutWithoutWaiting() throws InterruptedException {
        queue.put(42);
        Assert.assertEquals(1, queue.size());
    }

    // TODO: test blocking take.
    @Test
    public void testTake() throws InterruptedException {
        queue.put(42);
        Assert.assertTrue(queue.take() == 42);
    }

    @Test
    public void testReproduceBlockingRaceCondition() throws InterruptedException {
        int size = 100;

        CappedBlockingQueue<Runnable> runnableQueue = CappedBlockingQueue.<Runnable>builder()
                .build(size, buildArbitrarySortedSetDelegate());
        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(3, 3, 1, TimeUnit.MINUTES, runnableQueue);

        final CountDownLatch latch = new CountDownLatch(1);
        final Runnable sleepingRunnable = () -> {
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        for (int i = 0; i < size; i++) {
            threadPool.submit(sleepingRunnable);
        }

        latch.countDown(); // Start all the threads.

        threadPool.shutdown();
        while (!(threadPool.awaitTermination(1, TimeUnit.SECONDS))) {
            System.out.println("Remaining: " + threadPool.getQueue().size());
        }

    }

    @Test
    public void testReproduceTimeoutPutRaceCondition() throws InterruptedException {
        final CappedBlockingQueue<Integer> queue = this.queue;

        final CountDownLatch putterStarted = new CountDownLatch(1);
        Thread putter = new Thread(() -> {
            putterStarted.countDown();
            try {
                queue.put(42);
            } catch (InterruptedException e) {
                return;
            }
        });

        final CountDownLatch takerStarted = new CountDownLatch(1);
        Thread taker = new Thread(() -> {
            takerStarted.countDown();
            try {
                queue.take();
            } catch (InterruptedException e) {
                return;
            }
        });
        taker.start();
        Assert.assertTrue(takerStarted.await(1, TimeUnit.MINUTES)); // Make sure that the getter enters the lock first.
        putter.start();
        Assert.assertTrue(putterStarted.await(1, TimeUnit.MINUTES)); // Make sure that the getter enters the lock first.

        try {
            taker.join(10000);
            putter.join(10000);

            // Expect the two threads to be done after 2 seconds.
            Assert.assertFalse("Expected putter to be done since the queue was empty.", putter.isAlive());
            Assert.assertFalse("Expected the taker be done since the putter was supposed to put in an element.",
                    taker.isAlive());
        } finally {
            // Cleanup the threads.
            taker.interrupt();
            putter.interrupt();
            taker.join(10000);
            putter.join(10000);
            Assert.assertFalse(putter.isAlive());
            Assert.assertFalse(taker.isAlive());
        }
    }

    private static class MyComparable implements Comparable<MyComparable> {
        private int a;

        // private Random random = new Random(52);

        public MyComparable(int a) {
            this.a = a;
        }

        @Override
        public int compareTo(MyComparable o) {
            /*
             * if (random.nextInt() % 200 == 0) throw new RuntimeException("Could not add");
             */

            if (o.a == a) {
                return 0;
            }
            return o.a > a ? -1 : 1;
        }
    }

    @Test
    public void testRecreateAwaitingQueue() throws InterruptedException {
        final int CAP = 5;
        final CappedBlockingQueue<MyComparable> prioQueue = CappedBlockingQueue
                .<MyComparable>builder().build(CAP, buildNaturallySortedSetDelegate());

        final int N = 400000;

        Thread consumer = new Thread(() -> {
            Random random = new Random(42);

            for (int i = 0; i < CAP; i++) {
                if (random.nextInt(1000) == 0) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    prioQueue.take();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        consumer.start();

        Random random = new Random(42);
        int remainingToAdd = N;
        while (remainingToAdd > 0) {
            try {
                prioQueue.put(new MyComparable(random.nextInt()));
            } catch (RuntimeException e) {
                continue;
            }
            remainingToAdd--;
        }

        while (consumer.isAlive()) {
            System.out.println("Awaiting consumer to finish.");
            consumer.join(1000);
        }
        Assert.assertFalse(consumer.isAlive());
    }

    @Test
    public void testNegativeWaitTime() throws InterruptedException {
        PriorityBlockingQueue<MyComparable> prioQueue = new PriorityBlockingQueue<MyComparable>();
        prioQueue.poll(-5, TimeUnit.SECONDS);
    }

    @Test
    public void testEvictabilityOldAdded() throws InterruptedException {
        CappedBlockingQueue<Integer> evictionQueue = CappedBlockingQueue.<Integer>builder()
                .setIsEvictable(input -> input > 2).build(2, buildNaturallySortedSetDelegate());
        evictionQueue.add(3);
        evictionQueue.add(0);
        evictionQueue.add(1);
        Assert.assertEquals(2, evictionQueue.size());
        Assert.assertTrue(0 == evictionQueue.take());
        Assert.assertTrue(1 == evictionQueue.take());
        Assert.assertEquals(0, evictionQueue.size());
    }

    @Test
    public void testEvictabilityNewAdded() throws InterruptedException {
        CappedBlockingQueue<Integer> evictionQueue = CappedBlockingQueue.<Integer>builder()
                .setIsEvictable(input -> input > 2).build(2, buildNaturallySortedSetDelegate());
        Assert.assertTrue(evictionQueue.offer(1));
        Assert.assertTrue(evictionQueue.offer(2));
        Assert.assertFalse(evictionQueue.offer(3));
        Assert.assertEquals(2, evictionQueue.size());
        Assert.assertTrue(1 == evictionQueue.take());
        Assert.assertTrue(2 == evictionQueue.take());
        Assert.assertEquals(0, evictionQueue.size());
    }

    @Test
    public void testOfferingHighPrioFails() throws InterruptedException {
        CappedBlockingQueue<Integer> evictionQueue = CappedBlockingQueue.<Integer>builder()
                .setIsEvictable(input -> input > 2).build(2, buildNaturallySortedSetDelegate());
        Assert.assertTrue(evictionQueue.offer(1));
        Assert.assertTrue(evictionQueue.offer(2));
        Assert.assertFalse(evictionQueue.offer(1));
    }

    @Test
    @Ignore
    /**
     * Tries to recreate race condition that we had seen in production a couple of times. The race condition is that
     * only one thread is active and output will be like below
     * Remaining in queue 50. Active threads: 1
     * Remaining in queue 51. Active threads: 1
     * ....
     * Remaining in queue 100. Active threads: 1
     *
     * Ignored because it will run forever.
     */
    public void testReproduceEmptyQueueRaceCondition() throws InterruptedException {
        int size = 100;

        CappedBlockingQueue<Runnable> runnableQueue = CappedBlockingQueue.<Runnable>builder()
                .build(size, buildArbitrarySortedSetDelegate());

        final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(5, 5, 1, TimeUnit.MINUTES, runnableQueue);

        final Runnable sleepingRunnable = () -> {

            long sleep;

            int second = DateTime.now().getSecondOfMinute() / 10;

            // Quick 50% of time, slow 50% will simulate production behaviour
            if (second == 1 || second == 3 || second == 5) {
                sleep = (long) (700 * Math.random());
            } else {
                sleep = (long) (3000 * Math.random());
            }

            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        };

        Thread putter = new Thread(() -> {

            while (true) {
                try {
                    Thread.sleep((long) (500 * Math.random()));
                    threadPool.submit(sleepingRunnable);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        });

        Thread notificationThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                    System.out.println(String.format("Remaining in queue %s. Active threads: %s",
                            threadPool.getQueue().size(), threadPool.getActiveCount()));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        putter.start();
        notificationThread.start();

        while (!(threadPool.awaitTermination(10, TimeUnit.SECONDS))) {

        }
    }

    @Test
    public void testDrainToWithLimit() {
        CappedBlockingQueue<Integer> largerQueue = CappedBlockingQueue.<Integer>builder()
                .build(3, buildNaturallySortedSetDelegate());
        largerQueue.offer(0);
        largerQueue.offer(1);
        largerQueue.offer(2);

        Assert.assertEquals(3, largerQueue.size());

        ArrayList<Integer> output = Lists.newArrayList();
        largerQueue.drainTo(output, 1);

        Assert.assertEquals(2, largerQueue.size());
        Assert.assertEquals(1, output.size());
    }

    private static final Predicate<PrioritizedRunnable> IS_HIGH_PRIORITY_RUNNABLE = input ->
            input.priority <= PrioritizedRunnable.HIGH_PRIORITY;

    @Test
    @Ignore // Takes around 30 seconds to execute.
    public void testRunningDifferentPriorities() throws InterruptedException, ExecutionException, TimeoutException {
        WrappedRunnableListenableFutureTask.DelegateExtractor<PrioritizedRunnable> extractor = new
                WrappedRunnableListenableFutureTask
                        .DelegateExtractor<>();

        SortedDelegate<Integer, WrappedRunnableListenableFutureTask<PrioritizedRunnable, ?>> delegate = new
                SortedDelegate<>(Ordering.natural(), t -> t.getDelegate().priority);

        Predicate<WrappedRunnableListenableFutureTask<PrioritizedRunnable, ?>> evictableChecker = Predicates
                .compose(Predicates.not(IS_HIGH_PRIORITY_RUNNABLE), extractor);

        final CappedBlockingQueue build = CappedBlockingQueue.<WrappedRunnableListenableFutureTask<PrioritizedRunnable, ?>>builder()
                .setEvictionCallback(new PriorityExecutorQueueFactory.CancellingEvictionNotifier())
                .setIsEvictable(evictableChecker)
                .build(10, delegate);

        ThreadFactory TRANSACTIONS_CPU_THREAD_FACTORY = new ThreadFactoryBuilder()
                .setNameFormat("transaction-processor-cpu-thread-%d").build();

        ListenableThreadPoolExecutor<Runnable> cpuThreadPool = ListenableThreadPoolExecutor.builder(build,
                new TypedThreadPoolBuilder(4, TRANSACTIONS_CPU_THREAD_FACTORY))
                .withRejectedHandler(new TypeSafeBlockingExecutionHandler<>())
                .build();

        final RateLimiter rateLimiter = RateLimiter.create(40);
        final AtomicInteger atomicInteger = new AtomicInteger();
        final Runnable runnable = () -> {
            rateLimiter.acquire();
            System.out.println("Hello " + atomicInteger.getAndIncrement() + "!!");
        };

        final ArrayList<ListenableFuture<?>> runnables = Lists.newArrayList();
        for (int i = 0; i < 500; i++) {
            runnables.add(cpuThreadPool.execute(new PrioritizedRunnable(PrioritizedRunnable.LOW_PRIORITY, runnable)));
        }
        for (int i = 0; i < 500; i++) {
            runnables.add(cpuThreadPool.execute(new PrioritizedRunnable(PrioritizedRunnable.HIGH_PRIORITY, runnable)));
        }
        for (int i = 0; i < 500; i++) {
            runnables.add(cpuThreadPool.execute(new PrioritizedRunnable(PrioritizedRunnable.LOW_PRIORITY, runnable)));
        }

        for (ListenableFuture<?> listenableFuture : runnables) {
            try {
                System.out.println("Waiting for " + listenableFuture + " to finish.");
                listenableFuture.get(5, TimeUnit.MINUTES);
            } catch (CancellationException e) {
                // Ok
            }
        }

    }

}
