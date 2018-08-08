package se.tink.backend.queue.sqs;

import com.amazonaws.services.sqs.model.SendMessageRequest;
import se.tink.backend.queue.QueueProducer;

public class SqsProducer implements QueueProducer {

    private SqsQueue sqsQueue;

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
    public void send(String message) {
        SendMessageRequest sendMessageStandardQueue = new SendMessageRequest()
                .withQueueUrl(sqsQueue.getUrl())
                .withMessageBody(message)
                .withMessageAttributes(null);
        sqsQueue.getSqs().sendMessage(sendMessageStandardQueue);
    }
}