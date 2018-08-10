package se.tink.backend.queue.sqs.configuration;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown =  true)
public class SqsQueueConfiguration {
    @JsonProperty
    private String url;

    @JsonProperty
    private String region;

    @JsonProperty
    private String awsAccessKeyId;

    @JsonProperty
    private String awsSecretKey;

    @JsonProperty
    private String queueName;

    public SqsQueueConfiguration() {
    }

    public SqsQueueConfiguration(String url, String region, String awsAccessKeyId, String awsSecretKey, String queueName) {
        this.url = url;
        this.region = region;
        this.awsAccessKeyId = awsAccessKeyId;
        this.awsSecretKey = awsSecretKey;
        this.queueName = queueName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getAwsAccessKeyId() {
        return awsAccessKeyId;
    }

    public void setAwsAccessKeyId(String awsAccessKeyId) {
        this.awsAccessKeyId = awsAccessKeyId;
    }

    public String getAwsSecretKey() {
        return awsSecretKey;
    }

    public void setAwsSecretKey(String awsSecretKey) {
        this.awsSecretKey = awsSecretKey;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}
