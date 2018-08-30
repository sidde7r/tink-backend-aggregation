package se.tink.backend.queue.sqs;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import java.time.temporal.ValueRange;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.queue.QueueProducer;

import java.io.IOException;

public class SqsProducer implements QueueProducer {

    private SqsQueue sqsQueue;
    private EncodingHandler encodingHandler;
    private Logger logger = LoggerFactory.getLogger(SqsProducer.class);
    private static final ValueRange DELAY_MAX_RANGE = ValueRange.of(0, 900);

    @Inject
    public SqsProducer(SqsQueue sqsQueue, EncodingHandler encodingHandler) {
        this.sqsQueue = sqsQueue;
        this.encodingHandler = encodingHandler;
    }

    @Override
    public void send(Object t) {
        try {
            SendMessageRequest sendMessageStandardQueue = new SendMessageRequest()
                    .withQueueUrl(sqsQueue.getUrl())
                    .withMessageBody(encodingHandler.encode(t))
                    .withMessageAttributes(null); // FIXME: probably we want to use that in the future
            sqsQueue.getSqs().sendMessage(sendMessageStandardQueue);
            sqsQueue.produced();
        } catch (IOException e) {
            logger.error("Could not send message");
        }
        // TODO introduce metrics
    }

    // Future: requeue multiple jobs at the same time to reduce traffic
    @Override
    public void requeue(String sqsMessage) {
        SendMessageRequest sendMessageStandardQueue = new SendMessageRequest()
                .withQueueUrl(sqsQueue.getUrl())
                .withMessageBody(sqsMessage)
                //With delay seconds can max hide a message for 15 min. It
                .withDelaySeconds(randomTimeoutSeconds(0, 900));
        sqsQueue.getSqs().sendMessage(sendMessageStandardQueue);
        sqsQueue.reQueued();
        // TODO introduce metrics
    }

    public int randomTimeoutSeconds(int min, int max){
        Preconditions.checkArgument(min < max, "The minimum value cannot be same or equal to the maximum");
        Preconditions.checkArgument(DELAY_MAX_RANGE.isValidValue(min), "The minimum value is not in the allowed range");
        Preconditions.checkArgument(DELAY_MAX_RANGE.isValidValue(max), "The maximum value is not in the allowed range");

        return ThreadLocalRandom.current().nextInt(min, max);
    }

    @Override
    public boolean isAvailable() {
        return sqsQueue.isAvailable();
    }
}
