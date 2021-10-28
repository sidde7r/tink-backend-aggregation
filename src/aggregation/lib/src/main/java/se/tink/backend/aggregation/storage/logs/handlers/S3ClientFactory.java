package se.tink.backend.aggregation.storage.logs.handlers;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.google.inject.Inject;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.configuration.models.configuration.S3StorageConfiguration;

@RequiredArgsConstructor(onConstructor = @__({@Inject}))
public class S3ClientFactory {

    private final S3StorageConfiguration s3StorageConfiguration;

    public AmazonS3 createAWSClient() {
        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(
                                s3StorageConfiguration.getUrl(),
                                s3StorageConfiguration.getRegion()))
                .build();
    }
}
