package se.tink.backend.aggregation.sqs;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import org.junit.Test;

public class SqsTest {

    /**
     * Regression test to ensure that the following exception is not thrown when running the
     * aggregation service:
     *
     * <p>org.apache.http.conn.ssl.SSLConnectionSocketFactory.<init>(Ljavax/net/ssl/SSLContext;Ljavax/net/ssl/HostnameVerifier;)V
     * java.lang.NoSuchMethodError
     */
    @Test
    public void ensureSslMethodIsFoundAtRuntime() {
        final AmazonSQSClientBuilder amazonSQSClientBuilder =
                AmazonSQSClientBuilder.standard()
                        .withEndpointConfiguration(
                                new AwsClientBuilder.EndpointConfiguration(
                                        "http://localhost", "hoy"));

        final AWSCredentialsProvider provider =
                new AWSCredentialsProvider() {
                    @Override
                    public AWSCredentials getCredentials() {
                        return null;
                    }

                    @Override
                    public void refresh() {}
                };

        amazonSQSClientBuilder.withCredentials(provider).build();
    }
}
