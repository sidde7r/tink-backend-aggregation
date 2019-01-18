package se.tink.backend.aggregation.agents;

import com.google.common.collect.Maps;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import se.tink.backend.aggregation.api.AggregatorInfo;
import se.tink.libraries.metrics.MetricRegistry;

public abstract class AgentContext implements IAgentContext {
    private Map<String, Integer> transactionCountByEnabledAccount = Maps.newHashMap();
    protected ByteArrayOutputStream logOutputStream = new ByteArrayOutputStream();
    protected boolean isTestContext = false;
    private boolean isWaitingOnConnectorTransactions = false;
    private AggregatorInfo aggregatorInfo;
    private String clusterId;
    private MetricRegistry metricRegistry;


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

    public Map<String, Integer> getTransactionCountByEnabledAccount() {
        return transactionCountByEnabledAccount;
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }


}
