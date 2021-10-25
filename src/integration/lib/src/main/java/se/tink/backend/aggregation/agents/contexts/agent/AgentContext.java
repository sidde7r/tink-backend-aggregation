package se.tink.backend.aggregation.agents.contexts.agent;

import se.tink.backend.aggregation.agents.contexts.CompositeAgentContext;
import se.tink.backend.aggregation.agents.summary.refresh.RefreshSummary;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.logmasker.LogMasker;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.backend.aggregation.nxgen.http.event.event_producers.RawBankDataEventAccumulator;
import se.tink.backend.aggregation.nxgen.http.log.executor.aap.HttpAapLogger;
import se.tink.backend.aggregation.nxgen.http.log.executor.json.HttpJsonLogger;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;
import se.tink.libraries.credentials.service.RefreshableItem;
import se.tink.libraries.metrics.registry.MetricRegistry;
import se.tink.libraries.unleash.UnleashClient;
import src.libraries.interaction_counter.InteractionCounter;
import src.libraries.interaction_counter.local.LocalInteractionCounter;

public abstract class AgentContext implements CompositeAgentContext {

    protected boolean isTestContext = false;
    private boolean isWaitingOnConnectorTransactions = false;
    private AggregatorInfo aggregatorInfo;
    private String clusterId;
    private MetricRegistry metricRegistry;
    private String appId;
    private AgentConfigurationControllerable agentConfigurationController;
    private AgentsServiceConfiguration configuration;
    protected InteractionCounter supplementalInteractionCounter = new LocalInteractionCounter();
    private UnleashClient unleashClient;
    private String certId;
    private String providerId;
    protected RefreshSummary refreshSummary;
    protected AgentTemporaryStorage agentTemporaryStorage;
    private LogMasker logMasker;
    private HttpAapLogger httpAapLogger;
    private HttpJsonLogger httpJsonLogger;
    protected RawBankDataEventAccumulator rawBankDataEventAccumulator;
    protected String correlationId;
    private RefreshableItem refreshableItemInProgress;

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public InteractionCounter getSupplementalInteractionCounter() {
        return supplementalInteractionCounter;
    }

    public RefreshSummary getRefreshSummary() {
        return this.refreshSummary;
    }

    public void setRefreshSummary(RefreshSummary refreshSummary) {
        this.refreshSummary = refreshSummary;
    }

    @Override
    public String getAppId() {
        return appId;
    }

    @Override
    public void setAppId(String appId) {
        this.appId = appId;
    }

    @Override
    public String getClusterId() {
        return clusterId;
    }

    @Override
    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    @Override
    public AggregatorInfo getAggregatorInfo() {
        return aggregatorInfo;
    }

    @Override
    public void setAggregatorInfo(AggregatorInfo aggregatorInfo) {
        this.aggregatorInfo = aggregatorInfo;
    }

    @Override
    public boolean isTestContext() {
        return isTestContext;
    }

    @Override
    public void setTestContext(boolean isTestContext) {
        this.isTestContext = isTestContext;
    }

    @Override
    public boolean isWaitingOnConnectorTransactions() {
        return isWaitingOnConnectorTransactions;
    }

    @Override
    public void setWaitingOnConnectorTransactions(boolean waitingOnConnectorTransactions) {
        isWaitingOnConnectorTransactions = waitingOnConnectorTransactions;
    }

    @Override
    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    @Override
    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    @Override
    public AgentConfigurationControllerable getAgentConfigurationController() {
        return agentConfigurationController;
    }

    @Override
    public void setAgentConfigurationController(
            AgentConfigurationControllerable agentConfigurationController) {
        this.agentConfigurationController = agentConfigurationController;
    }

    @Override
    public UnleashClient getUnleashClient() {
        return this.unleashClient;
    }

    @Override
    public void setUnleashClient(UnleashClient unleashClient) {
        this.unleashClient = unleashClient;
    }

    @Override
    public String getCertId() {
        return certId;
    }

    @Override
    public void setCertId(String certId) {
        this.certId = certId;
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    @Override
    public AgentTemporaryStorage getAgentTemporaryStorage() {
        return agentTemporaryStorage;
    }

    @Override
    public void setAgentTemporaryStorage(AgentTemporaryStorage agentTemporaryStorage) {
        this.agentTemporaryStorage = agentTemporaryStorage;
    }

    @Override
    public LogMasker getLogMasker() {
        return logMasker;
    }

    @Override
    public void setLogMasker(LogMasker logMasker) {
        this.logMasker = logMasker;
    }

    @Override
    public HttpAapLogger getHttpAapLogger() {
        return httpAapLogger;
    }

    @Override
    public void setHttpAapLogger(HttpAapLogger httpAapLogger) {
        this.httpAapLogger = httpAapLogger;
    }

    @Override
    public HttpJsonLogger getHttpJsonLogger() {
        return httpJsonLogger;
    }

    @Override
    public void setHttpJsonLogger(HttpJsonLogger httpJsonLogger) {
        this.httpJsonLogger = httpJsonLogger;
    }

    @Override
    public void setRawBankDataEventAccumulator(
            RawBankDataEventAccumulator rawBankDataEventAccumulator) {
        this.rawBankDataEventAccumulator = rawBankDataEventAccumulator;
    }

    @Override
    public RawBankDataEventAccumulator getRawBankDataEventAccumulator() {
        return rawBankDataEventAccumulator;
    }

    @Override
    public void setCurrentRefreshableItemInProgress(RefreshableItem refreshableItem) {
        this.refreshableItemInProgress = refreshableItem;
    }

    @Override
    public RefreshableItem getCurrentRefreshableItemInProgress() {
        return this.refreshableItemInProgress;
    }
}
