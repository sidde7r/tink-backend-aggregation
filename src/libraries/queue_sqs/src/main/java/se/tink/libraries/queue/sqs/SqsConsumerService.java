package se.tink.libraries.queue.sqs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.dropwizard_lifecycle.ManagedSafeStop;
import se.tink.libraries.queue.QueueConsumerService;

public class SqsConsumerService extends ManagedSafeStop implements QueueConsumerService {

    private static final Logger log = LoggerFactory.getLogger(SqsConsumerService.class);
    // this is the ratio at which regular sqs queue will be interleaved with consumption from
    // priority retry queue
    private final float regularQueueInterleaveRatio;
    // this is the ratio at which retry sqs queue will be interleaved with consumption from
    // priority queue
    private final float retryQueueInterleaveRatio;
    private final Random random = new Random();

    private final AbstractExecutionThreadService service;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SqsConsumer regularSqsConsumer;
    private final SqsConsumer prioritySqsConsumer;
    private final SqsConsumer priorityRetrySqsConsumer;
    private final boolean consumeFromPriorityQueue;

    @Inject
    public SqsConsumerService(
            @Named("regularSqsConsumer") SqsConsumer regularSqsConsumer,
            @Named("prioritySqsConsumer") SqsConsumer prioritySqsConsumer,
            @Named("priorityRetrySqsConsumer") SqsConsumer priorityRetrySqsConsumer,
            AgentsServiceConfiguration agentsServiceConfiguration,
            @Named("regularQueueInterleaveRatio") float regularQueueInterleaveRatio,
            @Named("retryQueueInterleaveRatio") float retryQueueInterleaveRatio) {
        this.regularSqsConsumer = regularSqsConsumer;
        this.prioritySqsConsumer = prioritySqsConsumer;
        this.priorityRetrySqsConsumer = priorityRetrySqsConsumer;
        consumeFromPriorityQueue =
                agentsServiceConfiguration.isFeatureEnabled("consumeFromPriorityQueue");
        this.regularQueueInterleaveRatio = regularQueueInterleaveRatio;
        this.retryQueueInterleaveRatio = retryQueueInterleaveRatio;
        log.info(
                "Configured with consumeFromPriorityQueue={}, regularQueueInterleaveRatio={}, retryQueueInterleaveRatio={}",
                consumeFromPriorityQueue,
                regularQueueInterleaveRatio,
                retryQueueInterleaveRatio);
        this.service =
                new AbstractExecutionThreadService() {

                    @Override
                    protected void run() {
                        try {
                            // Ensure that we consume a maximum certain maximum of queue messages a
                            // second.
                            // Production rate will be way higher than this, leading to a long tail
                            // of the background refresh. Observed peak individual pods before this
                            // change has been noted to be around 5 consumptions/second.
                            RateLimiter rateLimiter = RateLimiter.create(0.8);
                            while (running.get()) {
                                rateLimiter.acquire();
                                consume();
                            }
                        } catch (Exception e) {
                            log.error(
                                    "Could not query, delete or consume for queue items: {}",
                                    e.getMessage());
                        }
                    }
                };

        // TODO introduce metrics
    }

    @VisibleForTesting
    void consume() throws IOException {
        boolean consumeFromRegularQueue = true;
        if (consumeFromPriorityQueue) {
            boolean priorityQueueEmpty = !prioritySqsConsumer.consume();
            boolean priorityRetryQueueEmpty = true;
            if (shouldConsumeFromPriorityRetryQueue(priorityQueueEmpty)) {
                priorityRetryQueueEmpty = !priorityRetrySqsConsumer.consume();
            }
            consumeFromRegularQueue =
                    shouldConsumeFromRegularQueue(priorityQueueEmpty, priorityRetryQueueEmpty);
        }
        if (consumeFromRegularQueue) {
            regularSqsConsumer.consume();
        }
    }

    private boolean shouldConsumeFromRegularQueue(
            boolean priorityQueueEmpty, boolean priorityRetryQueueEmpty) {
        // Consume from regular queue when:
        // - both priority queues are empty OR
        // - priorityQueue is empty but priorityRetryQueue is not & this is just a certain
        // fraction of workload (regularQueueInterleaveRatio)
        return priorityQueueEmpty && priorityRetryQueueEmpty
                || priorityQueueEmpty && random.nextFloat() <= regularQueueInterleaveRatio;
    }

    private boolean shouldConsumeFromPriorityRetryQueue(boolean priorityQueueEmpty) {
        // Consume from retry priority queue when:
        // - priority queue is empty OR
        // - priority queue is not empty & this is just a certain fraction of workload
        // (retryQueueInterleaveRatio).
        // Only fraction of workload is handled to achieve the following:
        // - minimize situations when priority queue and priority retry queue are consuming the same
        // provider at the same time
        // - help achieve better scattering of requests (so that providers are more interleaved)
        return priorityQueueEmpty || random.nextFloat() <= retryQueueInterleaveRatio;
    }

    @Override
    public void start() throws Exception {
        if (shouldStart()) {
            running.set(true);
            service.startAsync();
            service.awaitRunning(1, TimeUnit.MINUTES);
            log.info("SqsConsumerQueue started");
        }
    }

    private boolean shouldStart() {
        if (consumeFromPriorityQueue) {
            return regularSqsConsumer.isConsumerReady()
                    && prioritySqsConsumer.isConsumerReady()
                    && priorityRetrySqsConsumer.isConsumerReady();
        }
        return regularSqsConsumer.isConsumerReady();
    }

    @Override
    public void doStop() throws Exception {
        running.set(false);
        if (service.isRunning()) {
            service.awaitTerminated(30, TimeUnit.SECONDS);
        }
        log.info("SqsConsumerQueue stopped");
    }

    @VisibleForTesting
    boolean isRunning() {
        return running.get();
    }
}
