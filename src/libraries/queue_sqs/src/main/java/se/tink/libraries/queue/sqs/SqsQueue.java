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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.libraries.metrics.core.MetricId;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.metrics.types.counters.Counter;
import se.tink.libraries.queue.sqs.auth.RetryableInstanceProfileCredentialsProvider;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;

public class SqsQueue {
    private static final int[] BASE_2_ARRAY = {1, 2, 4, 8, 16, 32, 64};
    private static final int MINIMUM_SLEEP_TIME_IN_MILLISECONDS = 500;
    private final boolean isAvailable;
    private final String url;
    private Logger logger = LoggerFactory.getLogger(SqsQueue.class);
    private static final String LOCAL_REGION = "local";
    private static final MetricId METRIC_ID_BASE = MetricId.newId("aggregation_queues");
    private final Counter produced;
    private final Counter consumed;
    private final Counter requeued;
    private AmazonSQS sqs;

    @Inject
    public SqsQueue(SqsQueueConfiguration configuration, MetricRegistry metricRegistry) {
        this.consumed = metricRegistry.meter(METRIC_ID_BASE.label("event", "consumed"));
        this.produced = metricRegistry.meter(METRIC_ID_BASE.label("event", "produced"));
        this.requeued = metricRegistry.meter(METRIC_ID_BASE.label("event", "requeued"));

        if (!configuration.isEnabled()
                || Objects.isNull(configuration.getUrl())
                || Objects.isNull(configuration.getRegion())) {
            this.isAvailable = false;
            this.url = "";
            this.sqs = null;
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
            logger.info("We have a valid local configuration for fetching AWS credentials");
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
            logger.info("We don't have a valid local configuration for fetching AWS credentials");
            final AWSCredentialsProvider instanceCredentialsProvider =
                    RetryableInstanceProfileCredentialsProvider.createAsyncRefreshingProvider(true);

            this.url = configuration.getUrl();
            this.isAvailable =
                    isQueueCreated(
                            createRequest, amazonSQSClientBuilder, instanceCredentialsProvider);
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
                    logger.warn("Queue already exists.", e);
                }
                return true;
                // Reach this if the configurations are invalid
            } catch (SdkClientException e) {
                long backoffTime = calculateBackoffTime();
                logger.warn(
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
        this.consumed.inc();
    }

    public void produced() {
        this.produced.inc();
    }

    public void requeued() {
        this.requeued.inc();
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
