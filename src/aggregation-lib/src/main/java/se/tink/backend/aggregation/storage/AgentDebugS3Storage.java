package se.tink.backend.aggregation.storage;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.Bucket;
import java.io.File;
import java.io.IOException;
import com.google.inject.Inject;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.common.config.S3StorageConfiguration;


public class AgentDebugS3Storage implements AgentDebugStorageHandler {

    private static final Logger log = LoggerFactory.getLogger(AgentDebugS3Storage.class);
    private final S3StorageConfiguration configuration;
    private final AmazonS3 awsStorageClient;
    private final String bucketName;
    private boolean isAvailable = false;

    @Inject
    public AgentDebugS3Storage(S3StorageConfiguration configuration) {
        if (isValidConfiguration(configuration)) {
            this.configuration = configuration;
            this.bucketName = configuration.getAgentDebugBucketName();
            this.awsStorageClient = AmazonS3ClientBuilder.standard()
                    .withEndpointConfiguration(
                            new AwsClientBuilder.EndpointConfiguration(configuration.getUrl(),
                                    configuration.getRegion())
                    ).build();
            this.isAvailable = getBucket().isPresent();
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
    public String store(String content, File file) throws IOException {
        try {
            String fileName = file.getName();
            return putObject(content, fileName);
        } catch(AmazonServiceException e) {
            throw new IOException(e.getMessage());
        }
    }

    public static boolean isValidConfiguration(S3StorageConfiguration configuration) {
        return Objects.nonNull(configuration) &&
                configuration.isEnabled() &&
                Objects.nonNull(configuration.getUrl()) &&
                Objects.nonNull(configuration.getAgentDebugBucketName()) &&
                Objects.nonNull(configuration.getRegion());
    }

    public Optional<Bucket> getBucket() {
        if (awsStorageClient.doesBucketExist(bucketName)) {
            return fetchExistingBucket(bucketName);
        }

        try {
            return Optional.ofNullable(awsStorageClient.createBucket(bucketName));
        } catch (AmazonS3Exception e) {
            log.error(e.getErrorMessage());
        }

        return Optional.empty();
    }

    public Optional<Bucket> fetchExistingBucket(String bucketName) {
        return awsStorageClient.listBuckets().stream()
                .filter(bucket -> bucket.getName().equals(bucketName))
                .findFirst();
    }

    public String putObject(String content, String file) throws AmazonServiceException {
            awsStorageClient.putObject(bucketName, file, content);
            return awsStorageClient.getUrl(bucketName, file).toExternalForm();
    }
}
