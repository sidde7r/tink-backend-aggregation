package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import se.tink.backend.queue.sqs.configuration.SqsQueueConfiguration;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.endpoints.EndpointsConfiguration;

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
    private boolean isProvidersOnAggregation = false;

    @JsonProperty
    private SignatureKeyPair signatureKeyPair = new SignatureKeyPair();

    @JsonProperty
    private SqsQueueConfiguration sqsQueueConfiguration = new SqsQueueConfiguration();

    @JsonProperty
    private S3StorageConfiguration s3StorageConfiguration = new S3StorageConfiguration();

    @JsonProperty
    private ExcludedDebugClusters excludedDebugClusters = new ExcludedDebugClusters();

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

    public boolean isProvidersOnAggregation() {
        return isProvidersOnAggregation;
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
}
