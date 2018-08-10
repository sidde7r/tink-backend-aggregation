package se.tink.backend.queue.sqs;

import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xerial.snappy.Snappy;
import se.tink.backend.queue.QueueProducer;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.io.IOException;
import java.util.Base64;

public class SqsProducer implements QueueProducer {

    private SqsQueue sqsQueue;
    private Logger logger = LoggerFactory.getLogger(SqsProducer.class);

    @Inject
    public SqsProducer(SqsQueue sqsQueue) {
        this.sqsQueue = sqsQueue;
    }

    @Override
    public void start() {
    }

    @Override
    public void stop() {
        // not sure if it does what it implies
        sqsQueue.getSqs().shutdown();
    }

    @Override
    public void send(Object t) {
        SendMessageRequest sendMessageStandardQueue = null;
        try {
            sendMessageStandardQueue = new SendMessageRequest()
                    .withQueueUrl(sqsQueue.getUrl())
                    .withMessageBody(Base64.getEncoder().encodeToString(Snappy.compress(SerializationUtils.serializeToBinary(t))))
                    .withMessageAttributes(null);
            sqsQueue.getSqs().sendMessage(sendMessageStandardQueue);
        } catch (IOException e) {
            logger.error("Could not send message");
        }
    }

    @Override
    public boolean isAvailable() {
        return sqsQueue.isAvailable();
    }
}
