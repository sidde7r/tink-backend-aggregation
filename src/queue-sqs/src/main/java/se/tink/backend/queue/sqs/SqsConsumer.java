package se.tink.backend.queue.sqs;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import se.tink.backend.queue.QueueConsumer;
import se.tink.libraries.log.LogUtils;

public class SqsConsumer implements Managed, QueueConsumer {

    private final AbstractExecutionThreadService service;
    private final SqsQueue sqsQueue;
    private QueueMesssageAction queueMesssageAction;
    private final int WAIT_TIME_SECONDS = 1;
    private final int MAX_NUMBER_OF_MESSAGES = 1;
    private final int VISIBILITY_TIMEOUT_SECONDS = 300; //5 minutes
    private static final LogUtils log = new LogUtils(SqsConsumer.class);
    private AtomicBoolean running = new AtomicBoolean(false);


    @Inject
    public SqsConsumer(SqsQueue sqsQueue, QueueMesssageAction queueMesssageAction) {
        this.sqsQueue = sqsQueue;
        this.queueMesssageAction = queueMesssageAction;
        this.service = new AbstractExecutionThreadService() {

            @Override
            protected void run() {
                try {
                    while (running.get()) {
                        ReceiveMessageRequest request = createReceiveMessagesRequest();
                        List<Message> messages = readMessagesFromQueue(request);

                        for (Message message : messages) { // MAX_NUMBER_OF_MESSAGES is 1
                            delete(message);
                            tryConsumeUntilNotRejected(message);
                        }
                    }
                } catch (Exception e) {
                    log.error("Could not query, delete or consume for queue items: " + e.getMessage());
                }
            }
        };

        // TODO introduce metrics
    }

    private List<Message> readMessagesFromQueue(ReceiveMessageRequest request) {
        return sqsQueue.getSqs().receiveMessage(request).getMessages();
    }

    private ReceiveMessageRequest createReceiveMessagesRequest() {
        return new ReceiveMessageRequest(sqsQueue.getUrl())
                .withWaitTimeSeconds(WAIT_TIME_SECONDS)
                .withMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
                .withVisibilityTimeout(VISIBILITY_TIMEOUT_SECONDS);
    }

    private void tryConsumeUntilNotRejected(Message sqsMessage) throws Exception {
        int tries = 0;

        boolean consumed = false;
        while(!consumed) {
            try {
                consume(sqsMessage.getBody());
                consumed = true;
            } catch (RejectedExecutionException e) {
                Thread.sleep(50); // Wait 50ms to not spam either system
                log.info("Attempt (" + tries + ") to queue with message_id: " + sqsMessage.getMessageId());
                if (!running.get() && tries > 100) {
                    // If we are about to shutdown, don't retry-adding for more than 5000ms (sleep of 50ms times 100)
                    break;
                }
            }

            tries++;
        }
    }


    public void consume(String message) throws Exception {
        queueMesssageAction.handle(message);
    }

    public void delete(Message message){
        sqsQueue.getSqs().deleteMessage(new DeleteMessageRequest(sqsQueue.getUrl(), message.getReceiptHandle()));
    }

    @Override
    public void start() throws Exception {
        running.set(true);
        service.startAsync();
        service.awaitRunning(1, TimeUnit.MINUTES);
    }

    @Override
    public void stop() throws Exception {
        service.awaitTerminated(30, TimeUnit.SECONDS);
        running.set(false);
    }
}


