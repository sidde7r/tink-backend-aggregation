package se.tink.backend.aggregation.provider.configuration.cluster.providers;

import se.tink.backend.aggregation.provider.configuration.cluster.exceptions.ClusterNotValid;
import se.tink.backend.aggregation.provider.configuration.cluster.identifiers.ClusterId;
import se.tink.backend.aggregation.provider.configuration.cluster.identifiers.ClusterInfo;

public class ClusterIdProvider {

    public ClusterInfo getClusterInfo(String clusterName, String clusterEnvironment) throws ClusterNotValid {
        ClusterId clusterId = ClusterId.of(clusterName, clusterEnvironment);

        if (!clusterId.isValidId()) {
            throw new ClusterNotValid();
        }

        return ClusterInfo.of(clusterId);
    }
}
