package se.tink.backend.aggregation.configurations;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.common.config.DatabaseConfiguration;
import se.tink.backend.queue.sqs.configuration.SqsQueueConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceConfiguration extends Configuration {
    @JsonProperty
    private AgentsServiceConfiguration agentsServiceConfiguration = new AgentsServiceConfiguration();

    @JsonProperty
    private CacheConfiguration cache = new CacheConfiguration();

    @JsonProperty
    private CoordinationConfiguration coordination = new CoordinationConfiguration();

    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @JsonProperty
    private boolean developmentMode = false;

    @JsonProperty
    private PrometheusConfiguration prometheus = new PrometheusConfiguration();

    @JsonProperty
    private SqsQueueConfiguration sqsQueueConfiguration = new SqsQueueConfiguration();

    @JsonProperty
    private S3StorageConfiguration s3StorageConfiguration = new S3StorageConfiguration();

    @JsonProperty
    private AggregationDevelopmentConfiguration developmentConfiguration = new AggregationDevelopmentConfiguration();

    @JsonProperty
    private boolean isMultiClientDevelopment = false;

    public AgentsServiceConfiguration getAgentsServiceConfiguration() {
        return agentsServiceConfiguration;
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

    public PrometheusConfiguration getPrometheus() {
        return prometheus;
    }

    public SqsQueueConfiguration getSqsQueueConfiguration() {
        return sqsQueueConfiguration;
    }

    public S3StorageConfiguration getS3StorageConfiguration() {
        return s3StorageConfiguration;
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
