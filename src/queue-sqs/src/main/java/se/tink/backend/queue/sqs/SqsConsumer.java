package se.tink.backend.queue.sqs;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import java.util.HashMap;
import java.util.Map;
import se.tink.backend.queue.AutomaticRefreshStatus;
import se.tink.backend.queue.QueuableJob;
import se.tink.backend.queue.QueueConsumer;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SqsConsumer implements Managed, QueueConsumer {

    private final AbstractExecutionThreadService service;
    private final SqsQueue sqsQueue;
    private MessageHandler messageHandler;
    private final int WAIT_TIME_SECONDS = 1;
    private final int MAX_NUMBER_OF_MESSAGES = 1;
    private final Map<QueuableJob, Message> inProgress;
    private final int VISIBILITY_TIMEOUT_SECONDS = 300;


    @Inject
    public SqsConsumer(SqsQueue sqsQueue, MessageHandler messageHandler) {
        this.sqsQueue = sqsQueue;
        this.messageHandler = messageHandler;
        this.inProgress = new HashMap<QueuableJob, Message>();
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
                            //Prevent this message from being consumed by other clients until its removed or timeout
                            sqsQueue.getSqs().changeMessageVisibility(sqsQueue.getUrl(),
                                    sqsMessage.getReceiptHandle(),
                                    VISIBILITY_TIMEOUT_SECONDS);
                            QueuableJob consume = consume(sqsMessage.getBody());
                            inProgress.put(consume, sqsMessage);
                        }

                        removedFinishedJobs();

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

    public QueuableJob consume(String message) throws IOException {
        return messageHandler.handle(message);
    }

    public void removedFinishedJobs(){
        for(QueuableJob job : inProgress.keySet()){
            if(job.getStatus() == AutomaticRefreshStatus.SUCESS){
                deleteJob(job);
            }

            //Handle failures as before to not change behaviour. This should be changed at a further point
            //Add a timer for the job too, before removing
            if(job.getStatus() == AutomaticRefreshStatus.FAILED){
                deleteJob(job);
            }
        }
    }

    public void deleteJob(QueuableJob job){
        Message message = inProgress.get(job);
        sqsQueue.getSqs().deleteMessage(new DeleteMessageRequest(sqsQueue.getUrl(), message.getReceiptHandle()));
        inProgress.remove(job);
    }

}
