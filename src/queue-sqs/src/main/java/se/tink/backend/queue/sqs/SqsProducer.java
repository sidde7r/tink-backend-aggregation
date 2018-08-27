package se.tink.backend.queue.sqs;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.inject.Inject;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.queue.QueueProducer;

import java.io.IOException;

public class SqsProducer implements QueueProducer {

    private SqsQueue sqsQueue;
    private EncodingHandler encodingHandler;
    private Logger logger = LoggerFactory.getLogger(SqsProducer.class);

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
    public void requeue(Message sqsMessage) {
        SendMessageRequest sendMessageStandardQueue = new SendMessageRequest()
                .withQueueUrl(sqsQueue.getUrl())
                .withMessageBody(sqsMessage.getBody())
                //With delay seconds can max hide a message for 15 min. It
                .withDelaySeconds(randomTimeoutSeconds(0,900));
        sqsQueue.getSqs().sendMessage(sendMessageStandardQueue);
        sqsQueue.reQueued();
        // TODO introduce metrics
    }

    public int randomTimeoutSeconds(int min, int max){
        //This is the limits for delayed adding.
        //Could skip and change visibility for the message
        if(max > 900 || min >= max || min < 0){
            return ThreadLocalRandom.current().nextInt(0, 900);
        }

        return ThreadLocalRandom.current().nextInt(min, max);
    }

    @Override
    public boolean isAvailable() {
        return sqsQueue.isAvailable();
    }
}
