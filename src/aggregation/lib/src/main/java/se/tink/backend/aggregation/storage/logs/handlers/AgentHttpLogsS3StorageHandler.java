package se.tink.backend.aggregation.storage.logs.handlers;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.amazonaws.services.s3.AmazonS3;
import com.google.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.configuration.models.configuration.S3StorageConfiguration;
import se.tink.backend.aggregation.storage.logs.AgentHttpLogsStorageHandler;
import se.tink.backend.aggregation.storage.logs.handlers.AgentHttpLogsConstants.AgentDebugLogBucket;

@Slf4j
public class AgentHttpLogsS3StorageHandler implements AgentHttpLogsStorageHandler {

    private final S3StorageConfiguration s3StorageConfiguration;
    private final S3ClientFactory s3ClientFactory;

    private AmazonS3 awsStorageClient;

    @Inject
    public AgentHttpLogsS3StorageHandler(
            S3StorageConfiguration s3StorageConfiguration, S3ClientFactory s3ClientFactory) {
        this.s3StorageConfiguration = s3StorageConfiguration;
        this.s3ClientFactory = s3ClientFactory;
    }

    @Override
    public boolean isEnabled() {
        if (s3StorageConfiguration == null) {
            log.warn("S3 disabled - missing storage configuration");
            return false;
        }
        if (!s3StorageConfiguration.isEnabled()) {
            log.info("S3 disabled - no enabled flag");
            return false;
        }
        return checkForValidConfiguration();
    }

    /**
     * @param content - content of S3 log object
     * @param filePath - S3 log object file name
     * @param bucket - S3 bucket
     * @return On success, returns explanation of where log was stored. Otherwise, throws different
     *     types of errors.
     */
    @Override
    public String storeLog(String content, String filePath, AgentDebugLogBucket bucket) {
        if (!isEnabled()) {
            throw new IllegalStateException("Invalid attempt to use S3 storage - not enabled");
        }
        if (isBlank(filePath)) {
            throw new IllegalStateException(
                    "Invalid attempt to use S3 storage - object name not specified");
        }

        String bucketName = bucket.getBucketName(s3StorageConfiguration);
        putObjectInS3(content, filePath, bucketName);
        return String.format(
                "AWS CLI: s3://%s/%s\n" + "AWS HTTP: %s",
                bucketName,
                filePath,
                awsStorageClient.getUrl(bucketName, filePath).toExternalForm());
    }

    /*
    Unsuccessful request should cause exception to be thrown - we don't want to handle it here
     */
    private void putObjectInS3(String content, String fileName, String bucketName) {
        initAwsStorageClientLazily();
        awsStorageClient.putObject(bucketName, fileName, content);
    }

    private void initAwsStorageClientLazily() {
        if (awsStorageClient == null) {
            awsStorageClient = s3ClientFactory.createAWSClient();
        }
    }

    private boolean checkForValidConfiguration() {
        try {
            assertValidConfiguration();
        } catch (RuntimeException e) {
            log.error("Invalid S3 configuration", e);
            return false;
        }
        return true;
    }

    private void assertValidConfiguration() {
        boolean isS3ConnectionConfigured =
                Objects.nonNull(s3StorageConfiguration.getUrl())
                        && Objects.nonNull(s3StorageConfiguration.getRegion());
        if (!isS3ConnectionConfigured) {
            throw new IllegalStateException(
                    "Invalid S3 configuration - no configuration for connection");
        }

        List<AgentDebugLogBucket> bucketsWithoutName =
                Stream.of(AgentDebugLogBucket.values())
                        .filter(
                                bucket -> {
                                    String bucketName =
                                            bucket.getBucketName(s3StorageConfiguration);
                                    return StringUtils.isBlank(bucketName);
                                })
                        .collect(Collectors.toList());
        if (!bucketsWithoutName.isEmpty()) {
            throw new IllegalStateException(
                    "Invalid S3 configuration - no configuration for buckets: "
                            + bucketsWithoutName);
        }
    }
}
