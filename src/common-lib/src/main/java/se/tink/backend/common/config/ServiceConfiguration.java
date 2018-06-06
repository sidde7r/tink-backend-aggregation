package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import io.dropwizard.Configuration;
import java.util.List;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.endpoints.EndpointsConfiguration;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ServiceConfiguration extends Configuration {
    @JsonProperty
    private List<String> administrativeMode = Lists.newArrayList();

    @JsonProperty
    private AbnAmroConfiguration abnAmro = new AbnAmroConfiguration();

    @JsonProperty
    private AggregationWorkerConfiguration aggregationWorker = new AggregationWorkerConfiguration();

    @JsonProperty
    private IntegrationsConfiguration integrations = new IntegrationsConfiguration();

    @JsonProperty
    private CacheConfiguration cache = new CacheConfiguration();

    @JsonProperty
    private Cluster cluster;

    @JsonProperty
    private CoordinationConfiguration coordination = new CoordinationConfiguration();

    @JsonProperty("creditsafe")
    private CreditSafeConfiguration creditSafe = new CreditSafeConfiguration();

    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @JsonProperty
    private boolean developmentMode = false;

    @JsonProperty
    private EndpointsConfiguration endpoints = new EndpointsConfiguration();

    @JsonProperty
    private GrpcConfiguration grpc = new GrpcConfiguration();

    @JsonProperty
    private ServiceAuthenticationConfiguration serviceAuthentication = new ServiceAuthenticationConfiguration();

    private static final int YUBICO_CLIENT_ID = 11129;

    @JsonProperty
    private PrometheusConfiguration prometheus = new PrometheusConfiguration();

    @JsonProperty
    private boolean supplementalOnAggregation = false;

    @JsonProperty
    private boolean useAggregationController = false;

    @JsonProperty
    private boolean isAggregationCluster = false;

    @JsonProperty
    private boolean isProvidersOnAggregation = false;

    public AbnAmroConfiguration getAbnAmro() {
        return abnAmro;
    }

    public List<String> getAdministrativeMode() {
        return administrativeMode;
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

    public Cluster getCluster() {
        return cluster;
    }

    public CoordinationConfiguration getCoordination() {
        return coordination;
    }

    public DatabaseConfiguration getDatabase() {
        return database;
    }

    public EndpointsConfiguration getEndpoints() {
        return endpoints;
    }

    public GrpcConfiguration getGrpc() {
        return grpc;
    }

    public boolean isDevelopmentMode() {
        return developmentMode;
    }

    public CreditSafeConfiguration getCreditSafe() {
        return creditSafe;
    }

    public ServiceAuthenticationConfiguration getServiceAuthentication() {
        return serviceAuthentication;
    }

    public int getYubicoClientId() {
        return YUBICO_CLIENT_ID;
    }

    public void setCluster(Cluster cluster) {
        // TODO: remove this when new cluster names are merged in tink-infrastructure.
        if (cluster == Cluster.SEB) {
            this.cluster = Cluster.CORNWALL;
        } else {
            this.cluster = cluster;
        }
    }

    public PrometheusConfiguration getPrometheus() {
        return prometheus;
    }

    public boolean isSupplementalOnAggregation() {
        return supplementalOnAggregation;
    }

    public boolean isUseAggregationController() {
        return useAggregationController;
    }

    public boolean isAggregationCluster() {
        return isAggregationCluster;
    }

    public boolean isProvidersOnAggregation() {
        return isProvidersOnAggregation;
    }
}
