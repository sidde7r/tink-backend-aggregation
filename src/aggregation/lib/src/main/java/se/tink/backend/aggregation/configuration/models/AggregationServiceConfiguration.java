package se.tink.backend.aggregation.configuration.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.storage.file.models.ProvisionClientsConfig;
import se.tink.libraries.repository.config.DatabaseConfiguration;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregationServiceConfiguration extends Configuration {
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

    @JsonProperty
    private ProvisionClientsConfig provisionClientsConfig;

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

    public ProvisionClientsConfig getProvisionClientsConfig() {
        return provisionClientsConfig;
    }
}
