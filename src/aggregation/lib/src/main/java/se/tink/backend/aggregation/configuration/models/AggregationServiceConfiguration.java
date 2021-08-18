package se.tink.backend.aggregation.configuration.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.prometheus.client.CollectorRegistry;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.models.configuration.S3StorageConfiguration;
import se.tink.backend.aggregation.storage.file.models.ProvisionClientsConfig;
import se.tink.libraries.endpoints.dropwizard.EndpointsConfiguration;
import se.tink.libraries.queue.sqs.configuration.SqsQueueConfiguration;
import se.tink.libraries.repository.config.DatabaseConfiguration;
import se.tink.libraries.tracing.jaeger.models.JaegerConfig;
import se.tink.libraries.unleash.model.UnleashConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class AggregationServiceConfiguration extends Configuration {
    @JsonProperty
    private AgentsServiceConfiguration agentsServiceConfiguration =
            new AgentsServiceConfiguration();

    @JsonProperty private CacheConfiguration cache = new CacheConfiguration();

    @JsonProperty private CoordinationConfiguration coordination = new CoordinationConfiguration();

    @JsonProperty private DatabaseConfiguration database = new DatabaseConfiguration();

    @JsonProperty private boolean stagingEnvironment = false;

    @JsonProperty private boolean developmentMode = false;

    @JsonProperty private boolean decoupledMode = false;

    @JsonProperty private PrometheusConfiguration prometheus = new PrometheusConfiguration();

    // TODO change name here to sth like regularSqsQueueConfiguration
    @JsonProperty private SqsQueueConfiguration sqsQueueConfiguration = new SqsQueueConfiguration();

    @JsonProperty
    private SqsQueueConfiguration prioritySqsQueueConfiguration = new SqsQueueConfiguration();

    @JsonProperty
    private ProviderTierConfiguration providerTierConfiguration = new ProviderTierConfiguration();

    @JsonProperty
    private S3StorageConfiguration s3StorageConfiguration = new S3StorageConfiguration();

    @JsonProperty
    private AggregationDevelopmentConfiguration developmentConfiguration =
            new AggregationDevelopmentConfiguration();

    @JsonProperty private boolean isMultiClientDevelopment = false;

    @JsonProperty private JaegerConfig jaegerConfig;

    @JsonProperty private ProvisionClientsConfig provisionClientsConfig;

    @JsonProperty private EndpointsConfiguration endpoints = new EndpointsConfiguration();

    @JsonProperty
    private AccountInformationServiceConfiguration accountInformationService =
            new AccountInformationServiceConfiguration();

    @JsonProperty
    private ProviderConfigurationServiceConfiguration providerConfigurationServiceConfiguration =
            new ProviderConfigurationServiceConfiguration();

    @JsonProperty private UnleashConfiguration unleashConfig;

    @JsonProperty private boolean sendDataTrackingEvents = false;
    @JsonProperty private boolean sendAgentLoginCompletedEvents = false;
    @JsonProperty private boolean sendAgentRefreshEvents = false;
    @JsonProperty private boolean sendAccountInformationServiceEvents = false;
    @JsonProperty private boolean systemTestMode = false;

    // This should not be seen as part of configuration. It will be populated in the Application's
    // run method (AggregationServiceContainer).
    @JsonIgnore private CollectorRegistry collectorRegistry;

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

    public boolean isStagingEnvironment() {
        return stagingEnvironment;
    }

    public PrometheusConfiguration getPrometheus() {
        return prometheus;
    }

    public SqsQueueConfiguration getSqsQueueConfiguration() {
        return sqsQueueConfiguration;
    }

    public SqsQueueConfiguration getPrioritySqsQueueConfiguration() {
        return prioritySqsQueueConfiguration;
    }

    public S3StorageConfiguration getS3StorageConfiguration() {
        return s3StorageConfiguration;
    }

    public void setSqsQueueConfiguration(SqsQueueConfiguration sqsQueueConfiguration) {
        this.sqsQueueConfiguration = sqsQueueConfiguration;
    }

    public void setPrioritySqsQueueConfiguration(
            SqsQueueConfiguration prioritySqsQueueConfiguration) {
        this.prioritySqsQueueConfiguration = prioritySqsQueueConfiguration;
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

    public ProviderTierConfiguration getProviderTierConfiguration() {
        return providerTierConfiguration;
    }

    public ProviderConfigurationServiceConfiguration
            getProviderConfigurationServiceConfiguration() {
        return providerConfigurationServiceConfiguration;
    }

    public boolean isSendDataTrackingEvents() {
        return sendDataTrackingEvents;
    }

    public boolean isSendAgentLoginCompletedEvents() {
        return sendAgentLoginCompletedEvents;
    }

    public boolean isSendAgentRefreshEvents() {
        return sendAgentRefreshEvents;
    }

    public AccountInformationServiceConfiguration getAccountInformationService() {
        return accountInformationService;
    }

    public boolean isSendAccountInformationServiceEvents() {
        return sendAccountInformationServiceEvents;
    }

    public JaegerConfig getJaegerConfig() {
        return jaegerConfig;
    }

    public void setJaegerConfig(JaegerConfig jaegerConfig) {
        this.jaegerConfig = jaegerConfig;
    }

    public UnleashConfiguration getUnleashConfiguration() {
        if (unleashConfig != null) {
            log.info(
                    "[Unleash] The configuration was fetched successfully. Setting: [fetchToggleIntervals: `{} sec`, applicationName: `{}`]",
                    unleashConfig.getFetchFeatureToggleIntervals(),
                    unleashConfig.getApplicationName());
            return unleashConfig;
        }
        log.error("[Unleash] Something went wrong. Default configuration has been initialised.");
        return new UnleashConfiguration()
                .setApiUrl("http://unleash-api.unleash.svc.cluster.local:4242/api/")
                .setApplicationName("aggregation-service")
                .setFetchFeatureToggleIntervals(60);
    }

    public void setUnleashConfig(UnleashConfiguration unleashConfig) {
        this.unleashConfig = unleashConfig;
    }

    public void setCollectorRegistry(CollectorRegistry registry) {
        collectorRegistry = registry;
    }

    public CollectorRegistry getCollectorRegistry() {
        return collectorRegistry;
    }

    public boolean isSystemTestMode() {
        return systemTestMode;
    }
}
