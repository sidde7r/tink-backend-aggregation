package se.tink.backend.aggregation.provider.configuration.cluster.identifiers;

public class ClusterInfo {
    private final ClusterId clusterId;

    public ClusterInfo(ClusterId clusterId) {
        this.clusterId = clusterId;
    }

    public static ClusterInfo of(ClusterId clusterId) {
        return new ClusterInfo(clusterId);
    }

    public ClusterId getClusterId() {
        return clusterId;
    }
}
