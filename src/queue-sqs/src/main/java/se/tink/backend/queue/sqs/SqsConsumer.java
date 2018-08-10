package se.tink.backend.queue.sqs;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import org.xerial.snappy.Snappy;
import se.tink.backend.queue.QueueConsumer;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SqsConsumer implements Managed, QueueConsumer {

    private final AbstractExecutionThreadService service;
    private final SqsQueue sqsQueue;
    private MessageHandler messageHandler;
    private final int WAIT_TIME_SECONDS = 1;
    private final int MAX_NUMBER_OF_MESSAGES = 1;

    @Inject
    public SqsConsumer(SqsQueue sqsQueue, MessageHandler messageHandler) {
        this.sqsQueue = sqsQueue;
        this.messageHandler = messageHandler;
        this.service = new AbstractExecutionThreadService() {

            @Override
            protected void run() throws Exception {
                try {
                    while (true) {
                        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(sqsQueue.getUrl())
                                .withWaitTimeSeconds(WAIT_TIME_SECONDS)
                                .withMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES);
                        List<Message> sqsMessages = sqsQueue.getSqs().receiveMessage(receiveMessageRequest).getMessages();
                        for (Message sqsMessage : sqsMessages) {
                            consume(sqsMessage.getBody());
                            sqsQueue.getSqs().deleteMessage(new DeleteMessageRequest(sqsQueue.getUrl(), sqsMessage.getReceiptHandle()));
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Could not query for queue items.");
                }
            }

        };

        // TODO introduce metrics
    }

    @Override
    public void start() throws Exception {
        service.startAsync();
        service.awaitRunning(1, TimeUnit.MINUTES);
    }

    @Override
    public void stop() throws Exception {
        service.awaitTerminated(30, TimeUnit.SECONDS);
    }

    public void consume(String message) throws IOException {
        messageHandler.handle(Snappy.uncompress(Base64.getDecoder().decode(message)));
    }

}
