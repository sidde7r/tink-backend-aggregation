package se.tink.libraries.queue.sqs;

import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.dropwizard_lifecycle.ManagedSafeStop;
import se.tink.libraries.queue.QueueConsumerService;
import se.tink.libraries.queue.QueueProducer;

public class SqsConsumerService extends ManagedSafeStop implements QueueConsumerService {

    private final AbstractExecutionThreadService service;
    private final SqsQueue regularSqsQueue;
    private static final Logger log = LoggerFactory.getLogger(SqsConsumerService.class);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final SqsConsumer regularSqsConsumer;

    @Inject
    public SqsConsumerService(
            @Named("regularSqsQueue") SqsQueue regularSqsQueue,
            QueueMessageAction queueMessageAction,
            @Named("regularQueueProducer") QueueProducer regularQueueProducer) {
        this.regularSqsQueue = regularSqsQueue;
        this.regularSqsConsumer =
                new SqsConsumer(
                        regularSqsQueue, regularQueueProducer, queueMessageAction, "Regular");
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
                                regularSqsConsumer.consume();
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

    @Override
    public void start() throws Exception {
        if (regularSqsQueue.isAvailable()) {
            running.set(true);
            service.startAsync();
            service.awaitRunning(1, TimeUnit.MINUTES);
            log.info("SqsConsumerQueue started");
        }
    }

    @Override
    public void doStop() throws Exception {
        running.set(false);
        if (service.isRunning()) {
            service.awaitTerminated(30, TimeUnit.SECONDS);
        }
        log.info("SqsConsumerQueue stopped");
    }
}
