package se.tink.backend.aggregation.cluster.provider;

import se.tink.backend.aggregation.cluster.exception.ClusterNotValid;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.cluster.identification.ClusterInfo;

public class ClusterIdProvider {

    public ClusterInfo getClusterInfo(String clusterName, String clusterEnvironment) throws ClusterNotValid {
        ClusterId clusterId = ClusterId.of(clusterName, clusterEnvironment);

        if (!clusterId.isValidId()) {
            throw new ClusterNotValid();
        }

        return ClusterInfo.createForProviderConfigurationService(clusterId);
    }
}
