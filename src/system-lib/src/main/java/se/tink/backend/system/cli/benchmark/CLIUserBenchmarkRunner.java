package se.tink.backend.system.cli.benchmark;

import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;
import com.google.common.util.concurrent.RateLimiter;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import rx.Observable;
import se.tink.backend.common.repository.mysql.main.UserRepository;
import se.tink.backend.common.utils.RandomSample;
import se.tink.backend.core.User;
import se.tink.backend.system.cli.helper.traversal.ThreadPoolObserverTransformer;
import se.tink.backend.utils.LogUtils;

/**
 * Utility class to easily run benchmarks from a CLI. Useful if one would like to run some kind of benchmark where unit
 * tests can't be executed.
 */
public class CLIUserBenchmarkRunner {

    public static interface UserBenchmarker {
        void benchmark(User user);
    }

    private static final LogUtils log = new LogUtils(CLIUserBenchmarkRunner.class);
    private static final String ESSENTIALLY_NO_LIMIT = "50000";

    private final int numberOfUsersToBenchmark;
    private final UserRepository userRepository;
    private final ReentrantReadWriteLock latenciesSortLock = new ReentrantReadWriteLock();
    private final Double ratePerSecond;
    private final AtomicLong errors = new AtomicLong();
    private final AtomicLong progress = new AtomicLong();
    private final long repeatForSeconds;

    private CLIUserBenchmarkRunner(UserRepository userRepository) {
        this.ratePerSecond = Double.valueOf(System.getProperty("ratePerSecond", ESSENTIALLY_NO_LIMIT));
        this.numberOfUsersToBenchmark = Integer.getInteger("numberOfUsers", 100);
        this.repeatForSeconds = Long.getLong("repeatForSeconds", -1);
        this.userRepository = userRepository;
    }

    public void run(final UserBenchmarker userBenchmarker) {
        Preconditions
                .checkArgument(
                        progress.get() == 0,
                        "This class must be reinstantiated to be called again. It also doesn't support concurrent use. Please refactor to fix this if you need it.");

        // Must be synchronized.
        final Vector<Long> latencies = new Vector<>();

        final RateLimiter rateLimitter = RateLimiter.create(ratePerSecond);

        final List<String> userIdsToProcess = findUsersToProccess(numberOfUsersToBenchmark);

        // Measure latencies.

        Observable<String> userStream = Observable.from(userIdsToProcess);

        final boolean repeating = repeatForSeconds > 0;
        if (repeating) {
            final long lastExecution = System.currentTimeMillis()
                    + TimeUnit.MILLISECONDS.convert(repeatForSeconds, TimeUnit.SECONDS);
            userStream = userStream.repeat().takeWhile(userId -> System.currentTimeMillis() < lastExecution);
        }

        Stopwatch timer = Stopwatch.createStarted();

        userStream
                .map(userRepository::findOne)
                .compose(ThreadPoolObserverTransformer.buildFromSystemPropertiesWithConcurrency(20).<User> build())
                .forEach(user -> {
                    final String userId = user.getId();
                    try {

                        rateLimitter.acquire();

                        Stopwatch watch = Stopwatch.createStarted();

                        // Run the actual stuff to be tested.
                        userBenchmarker.benchmark(user);

                        long latency = watch.stop().elapsed(TimeUnit.MILLISECONDS);

                        // Using a read lock here because `latencies` is synchronized for adding, but not
                        // sorting.
                        ReadLock readLock = latenciesSortLock.readLock();
                        readLock.lock();
                        try {
                            latencies.add(latency);
                        } finally {
                            readLock.unlock();
                        }

                        long userCount = progress.incrementAndGet();
                        log.debug(
                                userId,
                                String.format(
                                        "Benchmark %d done out of %d users%s. Latency: %d ms",
                                        userCount, userIdsToProcess.size(),
                                        repeating ? " (repeating users if necessary)" : "", latency));

                        if (userCount % 500 == 0 && !latencies.isEmpty()) {
                            printStats(latencies);
                        }

                    } catch (Exception e) {
                        errors.incrementAndGet();
                        log.error(userId, "Could not benchmark.", e);
                    }
                });

        System.out.println(String.format("Test duration: %s", timer));

        if (!latencies.isEmpty()) {
            printStats(latencies);
        }
    }

    // Returning a list of userIds here to save some memory.
    private List<String> findUsersToProccess(int maxNumberOfUsersToBenchmark) {
        final Random random = new Random(Long.getLong("randomSeed", 42));
        ImmutableList<String> allUserIds = ImmutableList.copyOf(userRepository.streamAll()
                .map(User::getId).toBlocking().getIterator());

        if (maxNumberOfUsersToBenchmark <= 0) {
            // Allows us to run through all users.
            return allUserIds;
        }

        if (maxNumberOfUsersToBenchmark > allUserIds.size()) {
            log.warn(String.format("Sampling wasn't necessary. Only %d user were found. Asked to sample %d users.",
                    allUserIds.size(), maxNumberOfUsersToBenchmark));
            return allUserIds;
        }

        return RandomSample.from(allUserIds, random).pick(maxNumberOfUsersToBenchmark);
    }

    public static void printStats(String string, List<Long> latencies) {
        log.info(String.format("=========== %s statistics ==========", string));

        Collections.sort(latencies);
        printPercentile("Max", 1, latencies);
        printPercentile("99 perc", 0.99, latencies);
        printPercentile("95 perc", 0.95, latencies);
        printPercentile("90 perc", 0.9, latencies);
        printPercentile("50 perc", 0.5, latencies);
        printPercentile("10 perc", 0.1, latencies);
        printPercentile("5 perc", 0.05, latencies);
        printPercentile("1 perc", 0.01, latencies);
        printPercentile("Min", 0, latencies);

        long sum = 0;
        for (Long a : latencies) {
            sum += a;
        }
        printTableRow("Average", String.valueOf(1.0 * sum / latencies.size()), "ms");
    }

    private void printStats(List<Long> latencies) {
        WriteLock writeLock = latenciesSortLock.writeLock();
        writeLock.lock();
        try {
            printStats("Cassandra", latencies);

            printTableRow("Errors", String.valueOf(errors.get()));
            printTableRow("Total", String.valueOf(progress.get()));
        } finally {
            writeLock.unlock();
        }
    }

    private static void printPercentile(String string, double d, List<Long> latencies) {
        Preconditions.checkArgument(Ordering.natural().isOrdered(latencies));
        long index = Math.round(d * (latencies.size() - 1));
        printTableRow(string, String.valueOf(latencies.get((int) index)), "ms");
    }

    private static void printTableRow(String what, String value) {
        printTableRow(what, value, null);
    }

    private static void printTableRow(String what, String value, String unit) {
        log.info(String.format("%0$10s: %s%s", what, value, unit != null ? (" " + unit) : ""));
    }

    public static void run(UserRepository userRepository, UserBenchmarker userBenchmarker) {
        new CLIUserBenchmarkRunner(userRepository).run(userBenchmarker);
    }

}
