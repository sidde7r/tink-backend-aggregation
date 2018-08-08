package se.tink.backend.queue.sqs;

import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import se.tink.backend.queue.QueueConsumer;

import java.util.List;

public class SqsConsumer extends AbstractExecutionThreadService implements QueueConsumer {
    private SqsQueue sqsQueue;
    private final int WAIT_TIME_SECONDS = 1;
    private final int MAX_NUMBER_OF_MESSAGES = 3;

    public SqsConsumer(SqsQueue sqsQueue) {
        this.sqsQueue = sqsQueue;
    }

    @Override
    protected void startUp() throws Exception {
        super.startUp();
        startAsync();
    }

    @Override
    protected void run() throws Exception {
        while (isRunning()) {
            pollAndExecute();
        }
    }

    private void pollAndExecute() {
        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(sqsQueue.getUrl())
                .withWaitTimeSeconds(WAIT_TIME_SECONDS)
                .withMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES);
        List<Message> sqsMessages = sqsQueue.getSqs().receiveMessage(receiveMessageRequest).getMessages();

    }

    @Override
    protected void shutDown() throws Exception {
        super.shutDown();
    }
}
