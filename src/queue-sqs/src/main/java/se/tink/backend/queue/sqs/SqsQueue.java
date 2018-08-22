package se.tink.backend.queue.sqs;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.queue.sqs.configuration.SqsQueueConfiguration;

public class SqsQueue {

    private final AmazonSQS sqs;
    private final boolean isAvailable;
    private final String url;
    private Logger logger = LoggerFactory.getLogger(SqsQueue.class);
    private final static String LOCAL_REGION = "local";

    @Inject
    public SqsQueue(SqsQueueConfiguration configuration) {
        // Enable long polling when creating a queue
        CreateQueueRequest create_request = new CreateQueueRequest()
                .withQueueName(configuration.getQueueName())
                .addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");

        if(configuration.getRegion().equals(LOCAL_REGION)){
            sqs = AmazonSQSClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(configuration.getUrl(), configuration.getRegion()))
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                            configuration.getAwsAccessKeyId(),
                            configuration.getAwsSecretKey()
                    )))
                    .build();
        } else {
            sqs = AmazonSQSClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(configuration.getUrl(), configuration.getRegion())).build();
        }

        try {
            sqs.createQueue(create_request);
        } catch (AmazonSQSException e) {
            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                logger.warn("Queue already exists.");
            }
        } catch (SdkClientException e) {
            isAvailable = false;
            url = "";
            return;
        }

        isAvailable = true;

        GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(configuration.getQueueName());
        GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl(getQueueUrlRequest);
        this.url = getQueueUrlResult.getQueueUrl();

        // TODO: introrduce metrics
    }

    public AmazonSQS getSqs() {
        return sqs;
    }

    public String getUrl() {
        return url;
    }

    public boolean isAvailable() {
        return isAvailable;
    }
}
