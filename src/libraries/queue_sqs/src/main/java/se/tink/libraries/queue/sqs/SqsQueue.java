package se.tink.libraries.queue.sqs;

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
import com.google.common.util.concurrent.Uninterruptibles;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.metrics.Counter;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;

public class SqsQueue {
    private final AmazonSQS sqs;
    private final boolean isAvailable;
    private final String url;
    private Logger logger = LoggerFactory.getLogger(SqsQueue.class);
    private static final String LOCAL_REGION = "local";
    private static final MetricId METRIC_ID_BASE = MetricId.newId("aggregation_queues");
    private final Counter produced;
    private final Counter consumed;
    private final Counter rejected;

    @Inject
    public SqsQueue(SqsQueueConfiguration configuration, MetricRegistry metricRegistry) {
        this.consumed = metricRegistry.meter(METRIC_ID_BASE.label("event", "consumed"));
        this.produced = metricRegistry.meter(METRIC_ID_BASE.label("event", "produced"));
        this.rejected = metricRegistry.meter(METRIC_ID_BASE.label("event", "rejected"));

        if (!configuration.isEnabled()
                || Objects.isNull(configuration.getUrl())
                || Objects.isNull(configuration.getRegion())) {
            this.isAvailable = false;
            this.url = "";
            this.sqs = null;
            return;
        }

        // Enable long polling when creating a queue
        CreateQueueRequest createRequest =
                new CreateQueueRequest().addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");

        AmazonSQSClientBuilder amazonSQSClientBuilder =
                AmazonSQSClientBuilder.standard()
                        .withEndpointConfiguration(
                                new AwsClientBuilder.EndpointConfiguration(
                                        configuration.getUrl(), configuration.getRegion()));

        if (validLocalConfiguration(configuration)) {
            createRequest.withQueueName(configuration.getQueueName());

            sqs =
                    amazonSQSClientBuilder
                            .withCredentials(
                                    new AWSStaticCredentialsProvider(
                                            new BasicAWSCredentials(
                                                    configuration.getAwsAccessKeyId(),
                                                    configuration.getAwsSecretKey())))
                            .build();

            this.isAvailable = isQueueCreated(createRequest);
            this.url = this.isAvailable ? getQueueUrl(configuration.getQueueName()) : "";
        } else {
            sqs = amazonSQSClientBuilder.build();
            this.url = configuration.getUrl();
            this.isAvailable = isQueueCreated(createRequest);
        }
    }

    private String getQueueUrl(String name) {
        try {
            GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(name);
            GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl(getQueueUrlRequest);
            return getQueueUrlResult.getQueueUrl();
        } catch (AmazonSQSException e) {
            logger.warn("Queue configurations invalid", e);
            return "";
        }
    }

    // The retrying is necessary since the IAM access in Kubernetes is not instant.
    // The IAM access is necessary to get access to the queue.
    private boolean isQueueCreated(CreateQueueRequest createRequest) {
        int retries = 0;

        do {
            try {
                sqs.createQueue(createRequest);
                return true;
            } catch (AmazonSQSException e) {
                if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                    logger.warn("Queue already exists.", e);
                }
                return true;
                // Reach this if the configurations are invalid
            } catch (SdkClientException e) {
                logger.warn(
                        "No SQS with the current configurations is available, sleeping 1 second and then retrying.", e);
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
                retries++;
            }
        } while (retries < 10);

        throw new IllegalStateException("No SQS with the current configurations is available.");
    }

    private boolean validLocalConfiguration(SqsQueueConfiguration configuration) {
        return Objects.nonNull(configuration)
                && Objects.nonNull(configuration.getQueueName())
                && Objects.nonNull(configuration.getAwsAccessKeyId())
                && Objects.nonNull(configuration.getAwsSecretKey())
                && configuration.getRegion().equals(LOCAL_REGION);
    }

    public void consumed() {
        this.consumed.inc();
    }

    public void produced() {
        this.produced.inc();
    }

    public void rejected() {
        this.rejected.inc();
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
