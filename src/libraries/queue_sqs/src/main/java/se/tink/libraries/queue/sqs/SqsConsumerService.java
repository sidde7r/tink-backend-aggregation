package se.tink.libraries.queue.sqs;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.common.util.concurrent.RateLimiter;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.dropwizard_lifecycle.ManagedSafeStop;
import se.tink.libraries.queue.QueueConsumerService;
import se.tink.libraries.queue.QueueProducer;

public class SqsConsumerService extends ManagedSafeStop implements QueueConsumerService {

    private final AbstractExecutionThreadService service;
    private final SqsQueue sqsQueue;
    private final QueueMessageAction queueMessageAction;
    private static final int WAIT_TIME_SECONDS = 2;
    private static final int MAX_NUMBER_OF_MESSAGES = 1;
    private static final int VISIBILITY_TIMEOUT_SECONDS = 300; // 5 minutes
    private static final Logger log = LoggerFactory.getLogger(SqsConsumerService.class);
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final QueueProducer producer;

    @Inject
    public SqsConsumerService(
            @Named("regularSqsQueue") SqsQueue sqsQueue,
            QueueMessageAction queueMessageAction,
            @Named("regularQueueProducer") QueueProducer producer) {
        this.sqsQueue = sqsQueue;
        this.queueMessageAction = queueMessageAction;
        this.producer = producer;
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
                                ReceiveMessageRequest request = createReceiveMessagesRequest();
                                List<Message> messages = readMessagesFromQueue(request);

                                for (Message message : messages) { // MAX_NUMBER_OF_MESSAGES is 1
                                    delete(message);
                                    tryConsumeUntilNotRejected(message);
                                }
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

    private List<Message> readMessagesFromQueue(ReceiveMessageRequest request) {
        return sqsQueue.getSqs().receiveMessage(request).getMessages();
    }

    private ReceiveMessageRequest createReceiveMessagesRequest() {
        return new ReceiveMessageRequest(sqsQueue.getUrl())
                .withWaitTimeSeconds(WAIT_TIME_SECONDS)
                .withMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
                .withVisibilityTimeout(VISIBILITY_TIMEOUT_SECONDS);
    }

    private void tryConsumeUntilNotRejected(Message sqsMessage) throws Exception {
        try {
            consume(sqsMessage.getBody());
            sqsQueue.consumed();
        } catch (RejectedExecutionException e) {
            log.warn("Failed to consume message of SQS. Requeuing it.", e);
            producer.requeue(sqsMessage.getBody());
            sqsQueue.requeued();
        }
    }

    public void consume(String message) throws Exception {
        queueMessageAction.handle(message);
    }

    public void delete(Message message) {
        sqsQueue.getSqs()
                .deleteMessage(
                        new DeleteMessageRequest(sqsQueue.getUrl(), message.getReceiptHandle()));
    }

    @Override
    public void start() throws Exception {
        if (sqsQueue.isAvailable()) {
            running.set(true);
            service.startAsync();
            service.awaitRunning(1, TimeUnit.MINUTES);
            log.info("SqsConsumer started");
        }
    }

    @Override
    public void doStop() throws Exception {
        running.set(false);
        if (service.isRunning()) {
            service.awaitTerminated(30, TimeUnit.SECONDS);
        }
        log.info("SqsConsumer stopped");
    }
}
