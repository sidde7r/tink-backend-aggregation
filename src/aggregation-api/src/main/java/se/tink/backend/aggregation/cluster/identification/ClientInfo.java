package se.tink.backend.aggregation.cluster.identification;

public class ClientInfo {
    private String clientName;
    private String clusterId;
    private String aggregatorId;

    private ClientInfo(String clientName, String clusterId, String aggregatorId) {
        this.clientName = clientName;
        this.clusterId = clusterId;
        this.aggregatorId = aggregatorId;
    }

    public static ClientInfo of(String clientName, String clusterId, String aggregatorId) {
        return new ClientInfo(clientName, clusterId, aggregatorId);
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
}
