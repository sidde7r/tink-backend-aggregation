package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import io.dropwizard.Configuration;
import java.util.List;
import se.tink.libraries.abnamro.config.AbnAmroConfiguration;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.endpoints.EndpointsConfiguration;

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
    private AnalyticsConfiguration analytics = new AnalyticsConfiguration();

    @JsonProperty
    private ThreadPoolsConfiguration threadPools = new ThreadPoolsConfiguration();

    @JsonProperty
    private CacheConfiguration cache = new CacheConfiguration();

    @JsonProperty
    private Cluster cluster;

    @JsonProperty
    private ConnectorConfiguration connector = new ConnectorConfiguration();

    @JsonProperty
    private CoordinationConfiguration coordination = new CoordinationConfiguration();

    @JsonProperty("creditsafe")
    private CreditSafeConfiguration creditSafe = new CreditSafeConfiguration();

    @JsonProperty("idcontrol")
    private IDControlConfiguration idControl = new IDControlConfiguration();

    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @JsonProperty
    private boolean debugMode = false;

    @JsonProperty
    private boolean developmentMode = false;

    @JsonProperty
    private TemplateConfiguration template = new TemplateConfiguration();

    @JsonProperty
    private EndpointsConfiguration endpoints = new EndpointsConfiguration();

    @JsonProperty
    private FacebookConfiguration facebook = new FacebookConfiguration();

    @JsonProperty
    private FlagsConfiguration flags = new FlagsConfiguration();

    @JsonProperty
    private GrpcConfiguration grpc = new GrpcConfiguration();

    @JsonProperty
    private NotificationsConfiguration notifications = new NotificationsConfiguration();

    @JsonProperty
    private ReprocessTransactionsConfiguration reprocessTransactions = new ReprocessTransactionsConfiguration();

    @JsonProperty
    private TransfersConfiguration transfers = new TransfersConfiguration();

    @JsonProperty
    private boolean requireInjection = false;

    @JsonProperty
    private SchedulerConfiguration scheduler = new SchedulerConfiguration();

    @JsonProperty
    private SearchConfiguration search = new SearchConfiguration();

    @JsonProperty
    private ServiceAuthenticationConfiguration serviceAuthentication = new ServiceAuthenticationConfiguration();

    @JsonProperty
    private TwilioConfiguration twilio = new TwilioConfiguration();

    private static final int YUBICO_CLIENT_ID = 11129;

    @JsonProperty
    private TasksQueueConfiguration taskQueue = new TasksQueueConfiguration();

    @JsonProperty
    private PrometheusConfiguration prometheus = new PrometheusConfiguration();

    @JsonProperty
    private EmailConfiguration email = new EmailConfiguration();

    @JsonProperty
    private BackOfficeConfiguration backOffice = new BackOfficeConfiguration();

    @JsonProperty
    private boolean fraudCronEnabled = false;

    @JsonProperty
    private ActivitiesConfiguration activities = new ActivitiesConfiguration();

    @JsonProperty
    private AuthenticationConfiguration authentication = new AuthenticationConfiguration();

    @JsonProperty
    private CategorizationConfiguration categorization = new CategorizationConfiguration();

    @JsonProperty
    private TransactionConfiguration transaction = new TransactionConfiguration();

    @JsonProperty
    private TransactionProcessorConfiguration transactionProcessor = new TransactionProcessorConfiguration();

    @JsonProperty
    private StatisticConfiguration statistics = new StatisticConfiguration();

    @JsonProperty
    private InsightsConfiguration insights = new InsightsConfiguration();


    @JsonProperty
    private ExportUserDataConfiguration dataExport = new ExportUserDataConfiguration();

    @JsonProperty
    private FirehoseConfiguration firehose = new FirehoseConfiguration();

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

    public AnalyticsConfiguration getAnalytics() {
        return analytics;
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

    public ConnectorConfiguration getConnector() {
        return connector;
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

    public FacebookConfiguration getFacebook() {
        return facebook;
    }

    public FlagsConfiguration getFlags() {
        return flags;
    }

    public GrpcConfiguration getGrpc() {
        return grpc;
    }

    public InsightsConfiguration getInsightsConfiguration() {
        return insights;
    }

    public ExportUserDataConfiguration getDataExport() {
        return dataExport;
    }

    public FirehoseConfiguration getFirehose() {
        return firehose;
    }

    public NotificationsConfiguration getNotifications() {
        return notifications;
    }

    public ReprocessTransactionsConfiguration getReprocessTransactions() {
        return reprocessTransactions;
    }

    public ReprocessTransactionsConfiguration getReprocessTransactionsOptions() {
        return reprocessTransactions;
    }

    public SchedulerConfiguration getScheduler() {
        return scheduler;
    }

    public SchedulerConfiguration getSchedulerConfiguration() {
        return scheduler;
    }

    public SearchConfiguration getSearch() {
        return search;
    }

    public SearchConfiguration getSearchConfiguration() {
        return search;
    }

    public boolean isDevelopmentMode() {
        return developmentMode;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    public boolean isRequireInjection() {
        return requireInjection;
    }

    public CreditSafeConfiguration getCreditSafe() {
        return creditSafe;
    }

    public IDControlConfiguration getIdControl() {
        return idControl;
    }

    public ServiceAuthenticationConfiguration getServiceAuthentication() {
        return serviceAuthentication;
    }

    public int getYubicoClientId() {
        return YUBICO_CLIENT_ID;
    }

    public TasksQueueConfiguration getTaskQueue() {
        return taskQueue;
    }

    public ThreadPoolsConfiguration getThreadPools() {
        return threadPools;
    }

    public TransfersConfiguration getTransfers() {
        return transfers;
    }

    public void setTransfers(TransfersConfiguration transfers) {
        this.transfers = transfers;
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

    public TemplateConfiguration getTemplate() {
        return template;
    }

    public TwilioConfiguration getTwilio() {
        return twilio;
    }

    public EmailConfiguration getEmail() {
        return email;
    }

    public BackOfficeConfiguration getBackOffice() {
        return backOffice;
    }

    public boolean isFraudCronEnabled() {
        return fraudCronEnabled;
    }

    public ActivitiesConfiguration getActivities() {
        return activities;
    }

    public void setActivities(ActivitiesConfiguration activities) {
        this.activities = activities;
    }

    public AuthenticationConfiguration getAuthentication() {
        return authentication;
    }

    public CategorizationConfiguration getCategorization() {
        return categorization;
    }

    public TransactionConfiguration getTransaction() {
        return transaction;
    }

    public TransactionProcessorConfiguration getTransactionProcessor() {
        return transactionProcessor;
    }

    public StatisticConfiguration getStatistics() {
        return statistics;
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
