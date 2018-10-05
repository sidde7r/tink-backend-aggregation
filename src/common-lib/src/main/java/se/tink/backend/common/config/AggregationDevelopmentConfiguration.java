package se.tink.backend.common.config;

import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.ClusterHostConfiguration;

public class AggregationDevelopmentConfiguration {
    private ClusterHostConfiguration clusterHostConfiguration;
    private ClusterCryptoConfiguration clusterCryptoConfiguration;

    public ClusterHostConfiguration getClusterHostConfiguration() {
        return clusterHostConfiguration;
    }

    public ClusterCryptoConfiguration getClusterCryptoConfiguration() {
        return clusterCryptoConfiguration;
    }

    public boolean isValid() {
        if (this.clusterHostConfiguration == null) {
            return false;
        }

        if (!this.clusterHostConfiguration.isValid()) {
            return false;
        }

        if (this.clusterCryptoConfiguration == null) {
            return false;
        }

        if (!this.clusterCryptoConfiguration.isValid()) {
            return false;
        }

        return true;
    }
}
