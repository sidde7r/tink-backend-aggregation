package se.tink.backend.aggregation.cluster.identification;

public class ClusterInfo {
    private final ClusterId clusterId;
    private final String aggregationControllerHost;
    private final String apiToken;
    private final byte[] clientCertificate;
    private final boolean disableRequestCompression;

    private ClusterInfo(ClusterId clusterId, String aggregationControllerHost, String apiToken,
            byte[] clientCertificate, boolean disableRequestCompression) {
        this.clusterId = clusterId;
        this.aggregationControllerHost = aggregationControllerHost;
        this.apiToken = apiToken;
        this.clientCertificate = clientCertificate;
        this.disableRequestCompression = disableRequestCompression;

    }

    public static ClusterInfo createForAggregationCluster(ClusterId clusterId, String aggregationControllerHost,
            String apiToken, byte[] clientCertificate, boolean disableRequestCompression) {
        return new ClusterInfo(clusterId, aggregationControllerHost, apiToken, clientCertificate,
                disableRequestCompression);
    }

    public static ClusterInfo createForLegacyAggregation(ClusterId clusterId) {
        return new ClusterInfo(clusterId, null, null, null, false);
    }

    public ClusterId getClusterId() {
        return clusterId;
    }

    public String getAggregationControllerHost() {
        return aggregationControllerHost;
    }

    public String getApiToken() {
        return apiToken;
    }

    public byte[] getClientCertificate() {
        return clientCertificate;
    }

    public boolean isDisableRequestCompression() {
        return disableRequestCompression;
    }
}
