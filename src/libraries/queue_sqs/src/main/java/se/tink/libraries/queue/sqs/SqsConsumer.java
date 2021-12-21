package se.tink.libraries.queue.sqs;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.queue.QueueProducer;

public class SqsConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsConsumer.class);
    private static final int WAIT_TIME_SECONDS = 2;
    private static final int MAX_NUMBER_OF_MESSAGES = 1;
    private static final int VISIBILITY_TIMEOUT_SECONDS = 300; // 5 minutes

    private final SqsQueue sqsQueue;
    // Note: producer may queue requests to different sqs queue than this consumer reads from
    private final QueueProducer producer;
    private final QueueMessageAction queueMessageAction;
    private final String name;

    public SqsConsumer(
            SqsQueue sqsQueue,
            QueueProducer requeueProducer,
            QueueMessageAction queueMessageAction,
            String name) {
        this.sqsQueue = sqsQueue;
        this.producer = requeueProducer;
        this.queueMessageAction = queueMessageAction;
        this.name = name;
    }

    /**
     * Consumes messages from the Sqs queue
     *
     * @return false if there were no messages available** for that request, true - otherwise
     *     <p>** Please see
     *     https://docs.aws.amazon.com/AWSSimpleQueueService/latest/SQSDeveloperGuide/sqs-short-and-long-polling.html#sqs-long-polling
     *     for further information on why and when we may get no messages
     */
    public boolean consume() throws IOException {
        List<Message> messages = getMessages();
        if (messages.isEmpty()) {
            return false;
        }

        for (Message message : messages) { // MAX_NUMBER_OF_MESSAGES is 1
            delete(message);
            tryConsumeUntilNotRejected(message);
        }
        return true;
    }

    @VisibleForTesting
    List<Message> getMessages() {
        ReceiveMessageRequest request = createReceiveMessagesRequest();
        return sqsQueue.getSqs().receiveMessage(request).getMessages();
    }

    private ReceiveMessageRequest createReceiveMessagesRequest() {
        return new ReceiveMessageRequest(sqsQueue.getUrl())
                .withWaitTimeSeconds(WAIT_TIME_SECONDS)
                .withMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
                .withVisibilityTimeout(VISIBILITY_TIMEOUT_SECONDS);
    }

    @VisibleForTesting
    void tryConsumeUntilNotRejected(Message sqsMessage) throws IOException {
        try {
            consume(sqsMessage.getBody());
            sqsQueue.consumed();
        } catch (RejectedExecutionException e) {
            log.warn(
                    "[SqsConsumer] Failed to consume message from '{}' SQS. Requeuing it. SqsMessageId: {}",
                    name,
                    sqsMessage.getMessageId(),
                    e);
            producer.requeue(sqsMessage.getBody());
            sqsQueue.requeued();
        } catch (IOException e) {
            log.error(
                    "[SqsConsumer] Unexpected error happened during consuming of message. SqsMessageId: {}",
                    sqsMessage.getMessageId(),
                    e);
        }
    }

    @VisibleForTesting
    void consume(String message) throws IOException, RejectedExecutionException {
        queueMessageAction.handle(message);
    }

    @VisibleForTesting
    void delete(Message message) {
        log.info("[SqsConsumer] Deleting message: {}", message.getMessageId());
        sqsQueue.getSqs()
                .deleteMessage(
                        new DeleteMessageRequest(sqsQueue.getUrl(), message.getReceiptHandle()));
    }

    boolean isConsumerReady() {
        return sqsQueue.isAvailable();
    }
}
