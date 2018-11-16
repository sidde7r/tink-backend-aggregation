package se.tink.backend.aggregation.cluster.identification;

public class ClientInfo {
    private String clientName;
    private String apiClientKey;
    private String clusterId;
    private String aggregatorId;

    private ClientInfo(String clientName, String apiClientKey, String clusterId, String aggregatorId) {
        this.clientName = clientName;
        this.apiClientKey = apiClientKey;
        this.clusterId = clusterId;
        this.aggregatorId = aggregatorId;
    }

    public static ClientInfo of(String clientName, String apiClientKey, String clusterId, String aggregatorId) {
        return new ClientInfo(clientName, apiClientKey, clusterId, aggregatorId);
    }

    public String getClientName() {
        return clientName;
    }

    public String getApiClientKey() {
        return apiClientKey;
    }

    public String getClusterId() {
        return clusterId;
    }

    public String getAggregatorId() {
        return aggregatorId;
    }
}
