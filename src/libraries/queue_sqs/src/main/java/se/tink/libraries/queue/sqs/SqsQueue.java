package se.tink.libraries.queue.sqs;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentialsProvider;
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
import lombok.extern.slf4j.Slf4j;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.queue.sqs.auth.RetryableInstanceProfileCredentialsProvider;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;

@Slf4j
public class SqsQueue {
    private static final int[] BASE_2_ARRAY = {1, 2, 4, 8, 16, 32, 64};
    private static final int MINIMUM_SLEEP_TIME_IN_MILLISECONDS = 500;
    private static final String EVENT_LABEL = "event";
    private final boolean isAvailable;
    private final String url;
    private static final String LOCAL_REGION = "local";
    private static final MetricId METRIC_ID_BASE = MetricId.newId("aggregation_queues");
    private final String name;
    private final MetricRegistry metricRegistry;
    private AmazonSQS sqs;

    @Inject
    public SqsQueue(
            SqsQueueConfiguration configuration, MetricRegistry metricRegistry, String name) {
        this.name = name;
        this.metricRegistry = metricRegistry;

        if (!configuration.isEnabled()
                || Objects.isNull(configuration.getUrl())
                || Objects.isNull(configuration.getRegion())) {
            this.isAvailable = false;
            this.url = "";
            this.sqs = null;
            log.info(
                    "Improper sqs configuration - name: {}, enabled: {}, url: {}, region: {}",
                    configuration.getQueueName(),
                    configuration.isEnabled(),
                    configuration.getUrl(),
                    configuration.getRegion());
            return;
        }

        // Enable long polling when creating a queue
        final CreateQueueRequest createRequest =
                new CreateQueueRequest().addAttributesEntry("ReceiveMessageWaitTimeSeconds", "20");

        final AmazonSQSClientBuilder amazonSQSClientBuilder =
                AmazonSQSClientBuilder.standard()
                        .withEndpointConfiguration(
                                new AwsClientBuilder.EndpointConfiguration(
                                        configuration.getUrl(), configuration.getRegion()));

        if (validLocalConfiguration(configuration)) {
            log.info("We have a valid local configuration for fetching AWS credentials");
            createRequest.withQueueName(configuration.getQueueName());

            final AWSCredentialsProvider staticCredentialsProvider =
                    new AWSStaticCredentialsProvider(
                            new BasicAWSCredentials(
                                    configuration.getAwsAccessKeyId(),
                                    configuration.getAwsSecretKey()));

            this.isAvailable =
                    isQueueCreated(
                            createRequest, amazonSQSClientBuilder, staticCredentialsProvider);
            this.url = getQueueUrl(configuration.getQueueName());
        } else {
            log.info("We don't have a valid local configuration for fetching AWS credentials");
            final AWSCredentialsProvider instanceCredentialsProvider =
                    RetryableInstanceProfileCredentialsProvider.createAsyncRefreshingProvider(true);

            this.url = configuration.getUrl();
            this.isAvailable =
                    isQueueCreated(
                            createRequest, amazonSQSClientBuilder, instanceCredentialsProvider);
        }
        log.info("Queue is available: {}", this.isAvailable);
    }

    private String getQueueUrl(String name) {
        try {
            GetQueueUrlRequest getQueueUrlRequest = new GetQueueUrlRequest(name);
            GetQueueUrlResult getQueueUrlResult = sqs.getQueueUrl(getQueueUrlRequest);
            return getQueueUrlResult.getQueueUrl();
        } catch (AmazonSQSException e) {
            log.warn("Queue configurations invalid", e);
            return "";
        }
    }

    // The retrying is necessary since the IAM access in Kubernetes is not instant.
    // The IAM access is necessary to get access to the queue.
    private boolean isQueueCreated(
            CreateQueueRequest createRequest,
            AmazonSQSClientBuilder amazonSQSClientBuilder,
            AWSCredentialsProvider credentialsProvider) {
        do {
            try {
                // Not 100% sure that IAM is set up completely before this request is done. Hence
                // retry this on
                // every iteration to make sure credentials become available.
                this.sqs = amazonSQSClientBuilder.withCredentials(credentialsProvider).build();

                sqs.createQueue(createRequest);
                return true;
            } catch (AmazonSQSException e) {
                if (!e.getErrorCode().equals("QueueAlreadyExists")) {
                    log.warn("Queue already exists.", e);
                } else {
                    log.warn("Exception thrown when trying to create a queue", e);
                }
                return true;
                // Reach this if the configurations are invalid
            } catch (SdkClientException e) {
                long backoffTime = calculateBackoffTime();
                log.warn(
                        "No SQS with the current configurations is available, sleeping {} ms and then retrying.",
                        backoffTime,
                        e);

                Uninterruptibles.sleepUninterruptibly(backoffTime, TimeUnit.MILLISECONDS);
            }
        } while (true);
    }

    private static long calculateBackoffTime() {
        return (long) MINIMUM_SLEEP_TIME_IN_MILLISECONDS
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
        metricRegistry
                .meter(METRIC_ID_BASE.label(EVENT_LABEL, "consumed").label("name", name))
                .inc();
    }

    public void produced() {
        metricRegistry
                .meter(METRIC_ID_BASE.label(EVENT_LABEL, "produced").label("name", name))
                .inc();
    }

    public void requeued() {
        metricRegistry
                .meter(METRIC_ID_BASE.label(EVENT_LABEL, "requeued-rejected").label("name", name))
                .inc();
    }

    public void requeuedRateLimit() {
        metricRegistry
                .meter(METRIC_ID_BASE.label(EVENT_LABEL, "requeued-rate-limit").label("name", name))
                .inc();
    }

    public void expired() {
        metricRegistry
                .meter(METRIC_ID_BASE.label(EVENT_LABEL, "expired").label("name", name))
                .inc();
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
