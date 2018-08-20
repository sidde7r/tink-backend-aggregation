package se.tink.backend.queue.sqs;

import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.google.common.util.concurrent.AbstractExecutionThreadService;
import com.google.inject.Inject;
import io.dropwizard.lifecycle.Managed;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import se.tink.backend.queue.AutomaticRefreshStatus;
import se.tink.backend.queue.QueuableJob;
import se.tink.backend.queue.QueueConsumer;
import java.util.List;
import java.util.concurrent.TimeUnit;
import se.tink.libraries.log.LogUtils;

public class SqsConsumer implements Managed, QueueConsumer {

    private final AbstractExecutionThreadService service;
    private final SqsQueue sqsQueue;
    private MessageHandler messageHandler;
    private final int WAIT_TIME_SECONDS = 1;
    private final int MAX_NUMBER_OF_MESSAGES = 1;
    private final Map<QueuableJob, Message> inProgress;
    private final int VISIBILITY_TIMEOUT_SECONDS = 300; //5 minutes
    private static final LogUtils log = new LogUtils(SqsConsumer.class);
    private static final String VISIBLE_ITEMS_ATTRIBUTE = "ApproximateNumberOfMessages";
    private static final String HIDDEN_ITEMS_ATTRIBUTE = "ApproximateNumberOfMessagesNotVisible";

    @Inject
    public SqsConsumer(SqsQueue sqsQueue, MessageHandler messageHandler) {
        this.sqsQueue = sqsQueue;
        this.messageHandler = messageHandler;
        this.inProgress = new HashMap<QueuableJob, Message>();
        this.service = new AbstractExecutionThreadService() {

            @Override
            protected void run() {
                try {
                    while (true) {
                        ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(sqsQueue.getUrl())
                                .withWaitTimeSeconds(WAIT_TIME_SECONDS)
                                .withMaxNumberOfMessages(MAX_NUMBER_OF_MESSAGES)
                                .withVisibilityTimeout(VISIBILITY_TIMEOUT_SECONDS);
                        List<Message> sqsMessages = sqsQueue.getSqs().receiveMessage(receiveMessageRequest).getMessages();

                        for (Message sqsMessage : sqsMessages) {
                            /*sqsQueue.getSqs().changeMessageVisibility(sqsQueue.getUrl(),
                                    sqsMessage.getReceiptHandle(),
                                    VISIBILITY_TIMEOUT_SECONDS);*/
                            QueuableJob consume = consume(sqsMessage.getBody());
                            inProgress.put(consume, sqsMessage);
                        }

                        removedFinishedJobs();
                    }
                } catch (Exception e) {
                    log.error("Could not query for queue items: " + e.getMessage());
                }
            }
        };

        // TODO introduce metrics
    }


    public int getQueuedItems(String attribute){
        GetQueueAttributesRequest attributeRequest = new GetQueueAttributesRequest(sqsQueue.getUrl())
                .withAttributeNames(attribute);
        String result = sqsQueue.getSqs().getQueueAttributes(attributeRequest)
                .getAttributes().get(attribute);
        
        try{
            return Integer.parseInt(result);
        }catch(Exception e){
            return 0;
        }
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

    public QueuableJob consume(String message) throws Exception {
        return messageHandler.handle(message);
    }

    //Ensure sync
    public void removedFinishedJobs(){
        Iterator<Map.Entry<QueuableJob, Message>> jobIterator = inProgress.entrySet().iterator();
        while(jobIterator.hasNext()){
            Map.Entry<QueuableJob, Message> next = jobIterator.next();
            QueuableJob job = next.getKey();

            //Will try again, once the visibility timer expires
            //Should get rejected within 10 minutes
            //Request comes in --> tries to put on queue, if queue is empty it will execute with/without errors
            // --> if request is rejected, it will be deleted from "inProgress" but not from the message queue
            // The queue can be consumed 10 minutes later
            // Thus, it can try to queue another provider refresh, which might not have a full queue
            if (job.getStatus() == AutomaticRefreshStatus.REJECTED_BY_QUEUE) {
                jobIterator.remove();
            } else if (job.getStatus() == AutomaticRefreshStatus.SUCCESS) {
                deleteJob(job, jobIterator);
            } else if (job.getStatus() == AutomaticRefreshStatus.FAILED) {
                deleteJob(job, jobIterator);
            }
        }
    }

    public void deleteJob(QueuableJob job, Iterator<Map.Entry<QueuableJob, Message>> jobIterator){
        Message message = inProgress.get(job);
        sqsQueue.getSqs().deleteMessage(new DeleteMessageRequest(sqsQueue.getUrl(), message.getReceiptHandle()));
        jobIterator.remove();
    }

}
