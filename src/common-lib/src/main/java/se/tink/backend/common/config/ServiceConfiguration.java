package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import se.tink.backend.queue.sqs.configuration.SqsQueueConfiguration;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceConfiguration extends Configuration {
    @JsonProperty
    private AbnAmroConfiguration abnAmroStaging = new AbnAmroConfiguration();

    @JsonProperty
    private AbnAmroConfiguration abnAmroProduction = new AbnAmroConfiguration();

    @JsonProperty
    private AbnAmroConfiguration abnAmro = new AbnAmroConfiguration();

    @JsonProperty
    private AggregationWorkerConfiguration aggregationWorker = new AggregationWorkerConfiguration();

    @JsonProperty
    private IntegrationsConfiguration integrations = new IntegrationsConfiguration();

    @JsonProperty
    private CacheConfiguration cache = new CacheConfiguration();

    @JsonProperty
    private CoordinationConfiguration coordination = new CoordinationConfiguration();

    @JsonProperty("creditsafe")
    private CreditSafeConfiguration creditSafe = new CreditSafeConfiguration();

    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @JsonProperty
    private boolean developmentMode = false;

    @JsonProperty
    private PrometheusConfiguration prometheus = new PrometheusConfiguration();

    @JsonProperty
    private SignatureKeyPair signatureKeyPair = new SignatureKeyPair();

    @JsonProperty
    private SqsQueueConfiguration sqsQueueConfiguration = new SqsQueueConfiguration();

    @JsonProperty
    private S3StorageConfiguration s3StorageConfiguration = new S3StorageConfiguration();

    @JsonProperty
    private ExcludedDebugClusters excludedDebugClusters = new ExcludedDebugClusters();

    @JsonProperty
    private AggregationDevelopmentConfiguration developmentConfiguration = new AggregationDevelopmentConfiguration();

    @JsonProperty
    private boolean isMultiClientDevelopment = false;

    public AbnAmroConfiguration getAbnAmroStaging() {
        return abnAmroStaging;
    }

    public AbnAmroConfiguration getAbnAmroProduction() {
        return abnAmroProduction;
    }

    public AbnAmroConfiguration getAbnAmro() {
        return abnAmro;
    }

    public AggregationWorkerConfiguration getAggregationWorker() {
        return aggregationWorker;
    }

    public IntegrationsConfiguration getIntegrations() {
        return integrations;
    }

    public CacheConfiguration getCache() {
        return cache;
    }

    public CacheConfiguration getCacheConfiguration() {
        return cache;
    }

    public CoordinationConfiguration getCoordination() {
        return coordination;
    }

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public boolean isDevelopmentMode() {
        return developmentMode;
    }

    public CreditSafeConfiguration getCreditSafe() {
        return creditSafe;
    }

    public PrometheusConfiguration getPrometheus() {
        return prometheus;
    }

    public SignatureKeyPair getSignatureKeyPair() {
        return signatureKeyPair;
    }

    public SqsQueueConfiguration getSqsQueueConfiguration() {
        return sqsQueueConfiguration;
    }

    public S3StorageConfiguration getS3StorageConfiguration() {
        return s3StorageConfiguration;
    }

    public ExcludedDebugClusters getExcludedDebugClusters() {
        return excludedDebugClusters;
    }

    public void setSqsQueueConfiguration(SqsQueueConfiguration sqsQueueConfiguration) {
        this.sqsQueueConfiguration = sqsQueueConfiguration;
    }

    public AggregationDevelopmentConfiguration getDevelopmentConfiguration() {
        return developmentConfiguration;
    }

    public boolean isMultiClientDevelopment() {
        return isMultiClientDevelopment;
    }
}
