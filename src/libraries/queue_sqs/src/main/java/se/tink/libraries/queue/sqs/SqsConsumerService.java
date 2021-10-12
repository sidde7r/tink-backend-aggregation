package se.tink.libraries.queue.sqs;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.libraries.dropwizard_lifecycle.ManagedSafeStop;
import se.tink.libraries.queue.QueueConsumerService;

public class SqsConsumerService extends ManagedSafeStop implements QueueConsumerService {

    private final AbstractExecutionThreadService service;
    private static final Logger log = LoggerFactory.getLogger(SqsConsumerService.class);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SqsConsumer regularSqsConsumer;
    private final SqsConsumer prioritySqsConsumer;
    private final boolean consumeFromPriorityQueue;

    @Inject
    public SqsConsumerService(
            @Named("regularSqsConsumer") SqsConsumer regularSqsConsumer,
            @Named("prioritySqsConsumer") SqsConsumer prioritySqsConsumer,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        this.regularSqsConsumer = regularSqsConsumer;
        this.prioritySqsConsumer = prioritySqsConsumer;
        consumeFromPriorityQueue =
                agentsServiceConfiguration.isFeatureEnabled("consumeFromPriorityQueue");
        log.info("Configured with consumeFromPriorityQueue={}", consumeFromPriorityQueue);
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
            // consume from regular queue only if the priority queue is empty
            consumeFromRegularQueue = !prioritySqsConsumer.consume();
        }
        if (consumeFromRegularQueue) {
            regularSqsConsumer.consume();
        }
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
            return regularSqsConsumer.isConsumerReady() && prioritySqsConsumer.isConsumerReady();
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
