package se.tink.libraries.queue.sqs;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
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
    private final QueueProducer producer;
    private final QueueMessageAction queueMessageAction;
    private final String name;

    public SqsConsumer(
            SqsQueue sqsQueue,
            QueueProducer producer,
            QueueMessageAction queueMessageAction,
            String name) {
        this.sqsQueue = sqsQueue;
        this.producer = producer;
        this.queueMessageAction = queueMessageAction;
        this.name = name;
    }

    public void consume() throws IOException {
        List<Message> messages = getMessages();

        for (Message message : messages) { // MAX_NUMBER_OF_MESSAGES is 1
            delete(message);
            tryConsumeUntilNotRejected(message);
        }
    }

    private List<Message> getMessages() {
        ReceiveMessageRequest request = createReceiveMessagesRequest();
        return sqsQueue.getSqs().receiveMessage(request).getMessages();
    }

    private ReceiveMessageRequest createReceiveMessagesRequest() {
        return new ReceiveMessageRequest(sqsQueue.getUrl())
                .withWaitTimeSeconds(WAIT_TIME_SECONDS)
                .withMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
                .withVisibilityTimeout(VISIBILITY_TIMEOUT_SECONDS);
    }

    public void tryConsumeUntilNotRejected(Message sqsMessage) throws IOException {
        try {
            consume(sqsMessage.getBody());
            sqsQueue.consumed();
        } catch (RejectedExecutionException e) {
            log.warn("Failed to consume message of SQS. Requeuing it.", e);
            producer.requeue(sqsMessage.getBody());
            sqsQueue.requeued();
        }
    }

    public void consume(String message) throws IOException, RejectedExecutionException {
        queueMessageAction.handle(message);
    }

    public void delete(Message message) {
        sqsQueue.getSqs()
                .deleteMessage(
                        new DeleteMessageRequest(sqsQueue.getUrl(), message.getReceiptHandle()));
    }
}
