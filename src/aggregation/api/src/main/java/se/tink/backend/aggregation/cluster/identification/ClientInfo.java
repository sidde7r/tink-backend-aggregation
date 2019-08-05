package se.tink.backend.aggregation.cluster.identification;

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
}
