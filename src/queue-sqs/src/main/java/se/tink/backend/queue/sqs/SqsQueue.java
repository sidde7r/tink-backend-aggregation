package se.tink.backend.queue.sqs;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueUrlRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import se.tink.backend.common.config.SqsQueueConfiguration;

public class SqsQueue {

    private final AmazonSQS sqs;
    private final String url;

    public SqsQueue(SqsQueueConfiguration configuration) {
        this.sqs = AmazonSQSClientBuilder.standard()
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(configuration.getUrl(), configuration.getRegion()))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                        configuration.getAwsAccessKeyId(),
                        configuration.getAwsSecretKey()
                )))
                .build();
        GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(configuration.getQueueName());
        GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl(getQueueUrlRequest);
        this.url = getQueueUrlResult.getQueueUrl();
    }

    public AmazonSQS getSqs() {
        return sqs;
    }

    public String getUrl() {
        return url;
    }
}
