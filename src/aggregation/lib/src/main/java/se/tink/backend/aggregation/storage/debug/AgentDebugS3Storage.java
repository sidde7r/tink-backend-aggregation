package se.tink.backend.aggregation.storage.debug;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import java.io.File;
import java.io.IOException;
import com.google.inject.Inject;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.models.S3StorageConfiguration;


public class AgentDebugS3Storage implements AgentDebugStorageHandler {

    private static final Logger log = LoggerFactory.getLogger(AgentDebugS3Storage.class);
    private final S3StorageConfiguration configuration;
    private final AmazonS3 awsStorageClient;
    private final String bucketName;

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
        } else {
            log.error("S3 debug storage enabled, but configuration is not valid");
            this.configuration = null;
            this.awsStorageClient = null;
            this.bucketName = "";
        }
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

    @Override
    public boolean isLocalStorage() {
        return false;
    }

    private static boolean isValidConfiguration(S3StorageConfiguration configuration) {
        return Objects.nonNull(configuration) &&
                configuration.isEnabled() &&
                Objects.nonNull(configuration.getUrl()) &&
                Objects.nonNull(configuration.getAgentDebugBucketName()) &&
                Objects.nonNull(configuration.getRegion());
    }

    private String putObject(String content, String fileName) throws AmazonServiceException {
            awsStorageClient.putObject(bucketName, fileName, content);
            return String.format("AWS CLI: s3://%s/%s \n "
                            + "AWS HTTP: %s",
                    configuration.getAgentDebugBucketName(), fileName,
                    awsStorageClient.getUrl(bucketName, fileName).toExternalForm());
    }
}
