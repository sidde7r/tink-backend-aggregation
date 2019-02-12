package se.tink.libraries.queue.sqs;

import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.queue.QueueProducer;

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

    @Override
    public boolean isAvailable() {
        return sqsQueue.isAvailable();
    }
}
