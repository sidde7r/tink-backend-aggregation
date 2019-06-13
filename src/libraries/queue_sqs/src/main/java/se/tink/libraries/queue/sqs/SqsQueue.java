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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.metrics.MetricId;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;

public class SqsQueue {
    private static final Logger LOG = LoggerFactory.getLogger(SqsQueue.class);
    private static final int[] BASE_2_ARRAY = {1, 2, 4, 8, 16, 32, 64};
    private static final int MINIMUM_SLEEP_TIME_IN_MILLISECONDS = 500;
    private static final String LOCAL_REGION = "local";
    private static final MetricId METRIC_ID_BASE = MetricId.newId("aggregation_queues");
    private final SqsQueueConfiguration configuration;
    private final MetricRegistry metricRegistry;
    private AmazonSQS sqs;

    @Inject
    public SqsQueue(SqsQueueConfiguration configuration, MetricRegistry metricRegistry) {
        this.configuration = configuration;
        this.metricRegistry = metricRegistry;
    }

    private String getQueueUrl(String name) {
        try {
            GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(name);
            GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl(getQueueUrlRequest);
            return getQueueUrlResult.getQueueUrl();
        } catch (AmazonSQSException e) {
            LOG.warn("Queue configurations invalid", e);
            return "";
        }
    }

    // The retrying is necessary since the IAM access in Kubernetes is not instant.
    // The IAM access is necessary to get access to the queue.
    private void retryUntilCreated(
            CreateQueueRequest createRequest, AWSStaticCredentialsProvider credentialsProvider) {
        do {
            try {
                sqs.createQueue(createRequest);
                break;
            } catch (AmazonSQSException e) {
                if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                    LOG.warn("Queue already exists.", e);
                }
                break;
                // Reach this if the configurations are invalid
            } catch (SdkClientException e) {
                long backoffTime = calculateBackoffTime();
                LOG.warn(
                        "No SQS with the current configurations is available, sleeping {} ms and then retrying.",
                        backoffTime,
                        e);

                Uninterruptibles.sleepUninterruptibly(backoffTime, TimeUnit.MILLISECONDS);

                // Try to refresh the credentials
                if (Objects.isNull(credentialsProvider)) {
                    continue;
                }

                credentialsProvider.refresh();
            }
        } while (true);
    }

    private static long calculateBackoffTime() {
        return MINIMUM_SLEEP_TIME_IN_MILLISECONDS
                * BASE_2_ARRAY[ThreadLocalRandom.current().nextInt(BASE_2_ARRAY.length)];
    }

    private boolean validLocalConfiguration(SqsQueueConfiguration configuration) {
        return Objects.nonNull(configuration)
                && Objects.nonNull(configuration.getQueueName())
                && Objects.nonNull(configuration.getAwsAccessKeyId())
                && Objects.nonNull(configuration.getAwsSecretKey())
                && configuration.getRegion().equals(LOCAL_REGION);
    }

    public void consumed() {
        metricRegistry.meter(METRIC_ID_BASE.label("event", "consumed")).inc();
    }

    public void produced() {
        metricRegistry.meter(METRIC_ID_BASE.label("event", "produced")).inc();
    }

    public void rejected() {
        metricRegistry.meter(METRIC_ID_BASE.label("event", "rejected")).inc();
    }

    public AmazonSQS getSqs() {
        if (Objects.isNull(sqs)) {
            // Enable long polling when creating a queue
            CreateQueueRequest createRequest =
                    new CreateQueueRequest()
                            .addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");

            AmazonSQSClientBuilder amazonSQSClientBuilder =
                    AmazonSQSClientBuilder.standard()
                            .withEndpointConfiguration(
                                    new AwsClientBuilder.EndpointConfiguration(
                                            configuration.getUrl(), configuration.getRegion()));

            if (validLocalConfiguration(configuration)) {
                createRequest.withQueueName(configuration.getQueueName());

                AWSStaticCredentialsProvider credentialsProvider =
                        new AWSStaticCredentialsProvider(
                                new BasicAWSCredentials(
                                        configuration.getAwsAccessKeyId(),
                                        configuration.getAwsSecretKey()));

                sqs = amazonSQSClientBuilder.withCredentials(credentialsProvider).build();
                retryUntilCreated(createRequest, credentialsProvider);
            } else {
                sqs = amazonSQSClientBuilder.build();
                retryUntilCreated(createRequest, null);
            }
        }

        return sqs;
    }

    public String getUrl() {
        if (!isAvailable()) {
            return "";
        }

        if (!validLocalConfiguration(configuration)) {
            return configuration.getUrl();
        }

        return getQueueUrl(configuration.getQueueName());
    }

    public boolean isAvailable() {
        if (!configuration.isEnabled()) {
            return false;
        }

        if (Objects.isNull(configuration.getUrl())) {
            return false;
        }

        if (Objects.isNull(configuration.getRegion())) {
            return false;
        }

        return true;
    }
}
