package se.tink.backend.aggregation.s3storage;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import com.google.inject.Inject;
import java.util.Objects;
import se.tink.backend.common.config.S3StorageConfiguration;
import se.tink.backend.queue.sqs.configuration.SqsQueueConfiguration;

public class AgentDebugS3Storage implements AgentDebugStorageHandler {

    private final S3StorageConfiguration configuration;
    private final AmazonS3 awsStorageClient;
    private final String bucketName;
    private boolean isAvailable = false;

    @Inject
    public AgentDebugS3Storage(S3StorageConfiguration configuration) {
        if (isValidConfiguration(configuration)) {
            this.configuration = configuration;
            this.bucketName = configuration.getAgentDebugBucketName();
            awsStorageClient = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(configuration.getUrl(),
                                    configuration.getRegion())
                    ).build();
            this.isAvailable = Objects.nonNull(getBucket());
        } else {
            this.configuration = null;
            this.awsStorageClient = null;
            this.bucketName = "";
        }
    }

    @Override
    public boolean isAvailable() {
        return this.isAvailable;
    }

    @Override
    public String store(String name, String content) throws IOException {
        try {
            return putObject(name, content);
        } catch(AmazonServiceException e) {
            throw new IOException(e.getMessage());
        }
    }

    public boolean isValidConfiguration(S3StorageConfiguration configuration) {
        return Objects.nonNull(configuration) &&
                configuration.isEnabled() &&
                Objects.nonNull(configuration.getUrl()) &&
                Objects.nonNull(configuration.getRegion());
    }

    public Bucket getBucket() {
        Bucket b = null;
        if (awsStorageClient.doesBucketExist(bucketName)) {
            b = fetchExistingBucket(bucketName);
        } else {
            try {
                b = awsStorageClient.createBucket(bucketName);
            } catch (AmazonS3Exception e) {
                System.err.println(e.getErrorMessage());
            }
        }
        return b;
    }

    public Bucket fetchExistingBucket(String bucketName) {
        Bucket bucket = null;
        List<Bucket> buckets = awsStorageClient.listBuckets();
        for (Bucket b : buckets) {
            if (b.getName().equals(bucketName)) {
                bucket = b;
            }
        }
        return bucket;
    }

    public String putObject(String keyName, String content) throws AmazonServiceException {
            awsStorageClient.putObject(bucketName, keyName, content);
            return awsStorageClient.getUrl(bucketName, keyName).toExternalForm();
    }
}
