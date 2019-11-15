package se.tink.backend.aggregation.cluster.identification;

import com.google.common.base.Preconditions;

public class ClientInfo {
    private String clientName;
    private String clusterId;
    private String aggregatorId;
    private String appId;

    private ClientInfo(String clientName, String clusterId, String aggregatorId, String appId) {
        this.clientName = clientName;
        this.clusterId = clusterId;
        this.aggregatorId = aggregatorId;
        this.appId = appId;
    }

    public static ClientInfo of(
            String clientName, String clusterId, String aggregatorId, String appId) {
        return new ClientInfo(clientName, clusterId, aggregatorId, appId);
    }

    public String getClientName() {
        return clientName;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getAggregatorId() {
        return aggregatorId;
    }

    public String getAppId() {
        return appId;
    }

    public String getClusterName() {
        String[] split = clusterId.split("-");
        Preconditions.checkState(
                split.length == 2,
                "Trying to get the cluster name from an invalid clusterId : " + clusterId);
        return split[0];
    }

    public String getClusterEnvironment() {
        String[] split = clusterId.split("-");
        Preconditions.checkState(
                split.length == 2,
                "Trying to get the cluster environment from an invalid clusterId : " + clusterId);
        return split[1];
    }
}
