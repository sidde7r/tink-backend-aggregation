package se.tink.libraries.queue.sqs;

import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.queue.QueueProducer;

public class SqsProducer implements QueueProducer {

    private static final int REQUEUE_DELAY_MIN = 0;
    private static final int REQUEUE_DELAY_MAX = 180;

    private SqsQueue sqsQueue;
    private EncodingHandler encodingHandler;
    private Logger logger = LoggerFactory.getLogger(SqsProducer.class);

    public SqsProducer(SqsQueue sqsQueue, EncodingHandler encodingHandler) {
        this.sqsQueue = sqsQueue;
        this.encodingHandler = encodingHandler;
    }

    @Override
    public void send(Object t) {
        try {
            SendMessageRequest sendMessageStandardQueue =
                    new SendMessageRequest()
                            .withQueueUrl(sqsQueue.getUrl())
                            .withMessageBody(encodingHandler.encode(t))
                            .withMessageAttributes(
                                    null); // FIXME: probably we want to use that in the future
            SendMessageResult sendMessageResult =
                    sqsQueue.getSqs().sendMessage(sendMessageStandardQueue);
            logger.info(
                    "[SQSProducer] Requeued message with new id: {}",
                    sendMessageResult.getMessageId());
            sqsQueue.produced();
        } catch (IOException e) {
            logger.error("[SQSProducer] Could not send message", e);
        }
        // TODO introduce metrics
    }

    @Override
    public void requeue(String encodedMessageBody) {
        try {
            SendMessageRequest sendMessageStandardQueue =
                    new SendMessageRequest()
                            .withQueueUrl(sqsQueue.getUrl())
                            .withMessageBody(encodedMessageBody)
                            // With delay seconds can max hide a message for 15 min.
                            .withDelaySeconds(
                                    randomTimeoutSeconds(REQUEUE_DELAY_MIN, REQUEUE_DELAY_MAX));
            SendMessageResult sendMessageResult =
                    sqsQueue.getSqs().sendMessage(sendMessageStandardQueue);
            logger.info(
                    "[SQSProducer] Requeued message with new id: {}",
                    sendMessageResult.getMessageId());
        } catch (RuntimeException e) {
            logger.error("[SQSProducer] Couldn't requeue message", e);
        }
    }

    @Override
    public boolean isAvailable() {
        return sqsQueue.isAvailable();
    }

    private int randomTimeoutSeconds(int min, int max) {
        Preconditions.checkArgument(
                min < max, "The minimum value cannot be same or equal to the maximum");

        return ThreadLocalRandom.current().nextInt(min, max);
    }
}
