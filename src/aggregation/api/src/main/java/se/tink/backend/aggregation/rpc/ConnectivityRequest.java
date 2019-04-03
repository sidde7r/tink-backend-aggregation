package se.tink.backend.aggregation.rpc;

public class ConnectivityRequest {
    private String clusterId;

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
    }
}
