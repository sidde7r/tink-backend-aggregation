package se.tink.backend.aggregation.agents;

import com.google.common.collect.Maps;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.backend.aggregation.log.LogMasker;
import se.tink.backend.aggregation.nxgen.controllers.configuration.iface.AgentConfigurationControllerable;
import se.tink.libraries.metrics.registry.MetricRegistry;

public abstract class AgentContext implements CompositeAgentContext {
    protected ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
    protected boolean isTestContext = false;
    private Map<String, Integer> transactionCountByEnabledAccount = Maps.newHashMap();
    private boolean isWaitingOnConnectorTransactions = false;
    private AggregatorInfo aggregatorInfo;
    private String clusterId;
    private MetricRegistry metricRegistry;
    private String appId;
    private AgentConfigurationControllerable agentConfigurationController;
    private LogMasker logMasker;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }

    public ByteArrayOutputStream getLogOutputStream() {
        return logOutputStream;
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
    public void clear() {
        transactionCountByEnabledAccount.clear();
    }

    public boolean isTestContext() {
        return isTestContext;
    }

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
    public Map<String, Integer> getTransactionCountByEnabledAccount() {
        return transactionCountByEnabledAccount;
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
    public LogMasker getLogMasker() {
        return logMasker;
    }

    @Override
    public void setLogMasker(LogMasker logMasker) {
        this.logMasker = logMasker;
    }
}
