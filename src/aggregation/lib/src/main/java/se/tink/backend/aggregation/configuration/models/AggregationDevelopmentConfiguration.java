package se.tink.backend.aggregation.configuration.models;

import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;

public class AggregationDevelopmentConfiguration {
    private ClusterConfiguration clusterConfiguration;
    private ClientConfiguration clientConfiguration;
    private AggregatorConfiguration aggregatorConfiguration;
    private CryptoConfiguration cryptoConfiguration;

    public ClusterConfiguration getClusterConfiguration() {
        return clusterConfiguration;
    }

    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

    public AggregatorConfiguration getAggregatorConfiguration() {
        return aggregatorConfiguration;
    }

    public CryptoConfiguration getCryptoConfiguration() {
        return cryptoConfiguration;
    }

    public boolean isValid() {
        if (this.clusterConfiguration == null) {
            return false;
        }

        if (this.clientConfiguration == null) {
            return false;
        }

        if (this.cryptoConfiguration == null) {
            return false;
        }

        if (this.aggregatorConfiguration == null) {
            return false;
        }

        return true;
    }
}
