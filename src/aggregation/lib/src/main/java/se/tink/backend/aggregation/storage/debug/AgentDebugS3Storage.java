package se.tink.backend.aggregation.storage.debug;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.models.configuration.S3StorageConfiguration;

public class AgentDebugS3Storage implements AgentDebugStorageHandler {

    private static final Logger log = LoggerFactory.getLogger(AgentDebugS3Storage.class);
    private final S3StorageConfiguration configuration;
    private AmazonS3 awsStorageClient;

    @Inject
    public AgentDebugS3Storage(S3StorageConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String store(String content, File file) throws IOException {
        try {
            String fileName = file.getPath();
            return putObject(content, fileName);
        } catch (AmazonServiceException e) {
            throw new IOException(e.getMessage());
        }
    }

    @Override
    public boolean isLocalStorage() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return configuration.isEnabled();
    }

    private static boolean isValidConfiguration(S3StorageConfiguration configuration) {
        return Objects.nonNull(configuration)
                && Objects.nonNull(configuration.getUrl())
                && Objects.nonNull(configuration.getAgentDebugBucketName())
                && Objects.nonNull(configuration.getRegion());
    }

    private String putObject(String content, String fileName) throws AmazonServiceException {
        if (!isValidConfiguration(configuration)) {
            log.warn("S3 debug storage enabled, but configuration is not valid");
            return "";
        }

        // Instantiate the client lazily
        if (Objects.isNull(awsStorageClient)) {
            instantiateClient();
        }

        awsStorageClient.putObject(configuration.getAgentDebugBucketName(), fileName, content);
        return String.format(
                "AWS CLI: s3://%s/%s \n " + "AWS HTTP: %s",
                configuration.getAgentDebugBucketName(),
                fileName,
                awsStorageClient
                        .getUrl(configuration.getAgentDebugBucketName(), fileName)
                        .toExternalForm());
    }

    private void instantiateClient() {
        this.awsStorageClient =
                AmazonS3ClientBuilder.standard()
                        .withEndpointConfiguration(
                                new AwsClientBuilder.EndpointConfiguration(
                                        configuration.getUrl(), configuration.getRegion()))
                        .build();
    }
}
