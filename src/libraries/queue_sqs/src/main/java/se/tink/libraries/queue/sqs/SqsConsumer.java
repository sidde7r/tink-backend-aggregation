package se.tink.libraries.queue.sqs;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.queue.QueueProducer;
import se.tink.libraries.queue.sqs.configuration.SqsConsumerConfiguration;
import se.tink.libraries.queue.sqs.exception.ExpiredMessageException;
import se.tink.libraries.queue.sqs.exception.RateLimitException;

@Slf4j
public class SqsConsumer {
    public static final ImmutableList<Double> BUCKETS =
            ImmutableList.of(0., .025, .1, .25, .5, 0.75, 1., 1.5, 2., 2.5, 5.);
    private static final MetricId SQS_CONSUMER_DURATION_HISTOGRAM =
            MetricId.newId("sqs_consumption");

    private final SqsQueue sqsQueue;
    // Note: producer may queue requests to different sqs queue than this consumer reads from
    private final QueueProducer producer;
    private final QueueMessageAction queueMessageAction;
    private final String name;
    private final MetricRegistry metricRegistry;
    private final SqsConsumerConfiguration consumerConfig;

    public SqsConsumer(
            SqsQueue sqsQueue,
            QueueProducer requeueProducer,
            QueueMessageAction queueMessageAction,
            MetricRegistry metricRegistry,
            SqsConsumerConfiguration consumerConfig,
            String name) {
        this.sqsQueue = sqsQueue;
        this.producer = requeueProducer;
        this.queueMessageAction = queueMessageAction;
        this.metricRegistry = metricRegistry;
        this.consumerConfig = consumerConfig;
        this.name = name;
        log.info("Creating {} consumer with {}", name, consumerConfig);
    }

    /**
     * Consumes messages from the Sqs queue
     *
     * @return false if there were no messages available** for that request, true - otherwise
     *     <p>** Please see
     *     https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-short-and-long-polling.html#sqs-long-polling
     *     for further information on why and when we may get no messages
     */
    public boolean consume() {
        List<Message> messages = getMessages();
        if (messages.isEmpty()) {
            log.debug("[SqsConsumer] Messages empty");
            return false;
        }

        for (Message message : messages) { // MAX_NUMBER_OF_MESSAGES is 1
            log.debug("[SqsConsumer] Attempting to consume message: {}", message.getMessageId());
            delete(message);
            tryConsumeUntilNotRejected(message);
        }
        return true;
    }

    @VisibleForTesting
    List<Message> getMessages() {
        return timedMethod(
                "getMessages",
                () -> {
                    try {
                        ReceiveMessageRequest request = createReceiveMessagesRequest();
                        return sqsQueue.getSqs().receiveMessage(request).getMessages();
                    } catch (RuntimeException e) {
                        log.error("[SqsConsumer] Couldn't retrieve messages", e);
                        throw e;
                    }
                });
    }

    private ReceiveMessageRequest createReceiveMessagesRequest() {
        return new ReceiveMessageRequest(sqsQueue.getUrl())
                .withWaitTimeSeconds(consumerConfig.getWaitTimeSecond())
                .withMaxNumberOfMessages(consumerConfig.getMaxNumberOfMessages())
                .withVisibilityTimeout(consumerConfig.getVisibilityTimeoutSeconds());
    }

    @VisibleForTesting
    void tryConsumeUntilNotRejected(Message sqsMessage) {
        timedMethod(
                "tryConsumeUntilNotRejected",
                () -> {
                    try {
                        consume(sqsMessage.getBody());
                        sqsQueue.consumed();
                    } catch (RateLimitException e) {
                        log.debug(
                                "[SqsConsumer] Failed to consume message from '{}' SQS because provider was rate limmited. Requeuing it. SqsMessageId: {}",
                                name,
                                sqsMessage.getMessageId(),
                                e);
                        producer.requeueRateLimit(sqsMessage.getBody());
                    } catch (RejectedExecutionException e) {
                        log.debug(
                                "[SqsConsumer] Failed to consume message from '{}' SQS. Requeuing it. SqsMessageId: {}",
                                name,
                                sqsMessage.getMessageId(),
                                e);
                        producer.requeue(sqsMessage.getBody());
                    } catch (ExpiredMessageException expiredException) {
                        log.info(
                                "[SqsConsumer] Message with id: {} is expired.,",
                                sqsMessage.getMessageId(),
                                expiredException);
                        sqsQueue.expired();
                    } catch (IOException e) {
                        log.error(
                                "[SqsConsumer] Unexpected error happened during consuming of message. SqsMessageId: {}",
                                sqsMessage.getMessageId(),
                                e);
                    }
                    return null;
                });
    }

    @VisibleForTesting
    void consume(String message)
            throws IOException, RejectedExecutionException, RateLimitException,
                    ExpiredMessageException {
        queueMessageAction.handle(message);
    }

    @VisibleForTesting
    void delete(Message message) {
        timedMethod(
                "delete",
                () -> {
                    log.debug("[SqsConsumer] Deleting message: {}", message.getMessageId());
                    try {
                        sqsQueue.getSqs()
                                .deleteMessage(
                                        new DeleteMessageRequest(
                                                sqsQueue.getUrl(), message.getReceiptHandle()));
                    } catch (RuntimeException e) {
                        log.error("[SqsConsumer Failed to delete: {}", message.getMessageId(), e);
                    }
                    return null;
                });
    }

    boolean isConsumerReady() {
        return sqsQueue.isAvailable();
    }

    public <T> T timedMethod(String methodName, Callable<T> method) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            T result = method.call();
            registerDuration(methodName, "OK", stopwatch.elapsed(TimeUnit.MILLISECONDS));
            return result;
        } catch (Exception e) {
            registerDuration(
                    methodName,
                    "UNKNOWN_EXCEPTION",
                    stopwatch.stop().elapsed(TimeUnit.MILLISECONDS));
            throw new IllegalStateException(
                    String.format(
                            "Error encountered when calling method %s in SqsConsumer", methodName),
                    e);
        }
    }

    private void registerDuration(String methodName, String status, long durationMs) {
        metricRegistry
                .histogram(
                        SQS_CONSUMER_DURATION_HISTOGRAM
                                .label("consumer", name)
                                .label("methodName", methodName)
                                .label("status", status),
                        BUCKETS)
                .update(durationMs / 1000.0);
    }
}
