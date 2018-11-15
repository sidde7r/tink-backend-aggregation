package se.tink.backend.aggregation.configuration.models;

import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.ClusterHostConfiguration;

public class AggregationDevelopmentConfiguration {
    private ClusterHostConfiguration clusterHostConfiguration;
    private ClusterCryptoConfiguration clusterCryptoConfiguration;
    private ClusterConfiguration clusterConfiguration;

    public ClusterHostConfiguration getClusterHostConfiguration() {
        return clusterHostConfiguration;
    }

    public ClusterCryptoConfiguration getClusterCryptoConfiguration() {
        return clusterCryptoConfiguration;
    }

    public ClusterConfiguration getClusterConfiguration() { return clusterConfiguration;}

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

        if (this.clusterConfiguration == null) {
            return false;
        }

        return true;
    }
}
