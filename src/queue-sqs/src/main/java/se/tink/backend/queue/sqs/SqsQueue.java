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
import java.util.Objects;
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
        if (configuration.isEnabled() == false) {
            this.isAvailable = false;
            this.url = "";
            this.sqs = null;
            return;
        }

        CreateQueueRequest createRequest = new CreateQueueRequest().addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");

        AmazonSQSClientBuilder amazonSQSClientBuilder = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(configuration.getUrl(), configuration.getRegion()));

        if (configuration.getRegion().equals(LOCAL_REGION) && validLocalConfiguration(configuration)) {
            createRequest.withQueueName(configuration.getQueueName());

            sqs = amazonSQSClientBuilder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                    configuration.getAwsAccessKeyId(),
                    configuration.getAwsSecretKey()
            )))
                    .build();

            this.isAvailable = isQueueAvailable(createRequest);
            this.url = this.isAvailable ? getQueueUrl(configuration.getQueueName()) : "";
        } else {
            sqs = amazonSQSClientBuilder.build();
            this.url = configuration.getUrl();
            this.isAvailable = isQueueAvailable(createRequest);
        }

        // TODO: introrduce metrics
    }

    private String getQueueUrl(String name){
        try {
            GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(name);
            GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl(getQueueUrlRequest);
            return getQueueUrlResult.getQueueUrl();
        } catch (AmazonSQSException e) {
            logger.warn("Queue configurations invalid");
            return "";
        }
    }

    private boolean isQueueAvailable(CreateQueueRequest create_request){
        try {
            sqs.createQueue(create_request);
        } catch (AmazonSQSException e) {
            if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                logger.warn("Queue already exists.");
            }
        } catch (SdkClientException e) {
            return false;
        }

        return true;
    }

    public boolean validLocalConfiguration(SqsQueueConfiguration configuration){
        return Objects.nonNull(configuration) &&
                Objects.nonNull(configuration.getQueueName()) &&
                Objects.nonNull(configuration.getRegion()) &&
                Objects.nonNull(configuration.getAwsAccessKeyId()) &&
                Objects.nonNull(configuration.getAwsSecretKey());
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
