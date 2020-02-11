package se.tink.backend.aggregation.configuration.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.storage.file.models.ProvisionClientsConfig;
import se.tink.libraries.endpoints.dropwizard.EndpointsConfiguration;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;
import se.tink.libraries.repository.config.DatabaseConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AggregationServiceConfiguration extends Configuration {
    @JsonProperty
    private AgentsServiceConfiguration agentsServiceConfiguration =
            new AgentsServiceConfiguration();

    @JsonProperty private CacheConfiguration cache = new CacheConfiguration();

    @JsonProperty private CoordinationConfiguration coordination = new CoordinationConfiguration();

    @JsonProperty private DatabaseConfiguration database = new DatabaseConfiguration();

    @JsonProperty private boolean developmentMode = false;

    @JsonProperty private boolean decoupledMode = false;

    @JsonProperty private PrometheusConfiguration prometheus = new PrometheusConfiguration();

    @JsonProperty private SqsQueueConfiguration sqsQueueConfiguration = new SqsQueueConfiguration();

    @JsonProperty
    private S3StorageConfiguration s3StorageConfiguration = new S3StorageConfiguration();

    @JsonProperty
    private AggregationDevelopmentConfiguration developmentConfiguration =
            new AggregationDevelopmentConfiguration();

    @JsonProperty private boolean isMultiClientDevelopment = false;

    @JsonProperty private ProvisionClientsConfig provisionClientsConfig;

    @JsonProperty private EndpointsConfiguration endpoints = new EndpointsConfiguration();

    @JsonProperty
    private ProviderConfigurationServiceConfiguration providerConfigurationServiceConfiguration =
            new ProviderConfigurationServiceConfiguration();

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

    /**
     * If true, the service will be run with all external services (including mysql, memcached etc.)
     * faked away. Takes precedence over Development Mode.
     */
    public boolean isDecoupledMode() {
        return decoupledMode;
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

    public EndpointsConfiguration getEndpoints() {
        return endpoints;
    }

    public ProviderConfigurationServiceConfiguration
            getProviderConfigurationServiceConfiguration() {
        return providerConfigurationServiceConfiguration;
    }
}
