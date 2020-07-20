package se.tink.backend.aggregation.configuration.models;

import se.tink.backend.aggregation.storage.database.models.AggregationControllerClientConfiguration;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;

public class AggregationDevelopmentConfiguration {
    private ClusterConfiguration clusterConfiguration;
    private ClientConfiguration clientConfiguration;
    private AggregatorConfiguration aggregatorConfiguration;
    private CryptoConfiguration cryptoConfiguration;
    private AggregationControllerClientConfiguration aggregationControllerClientConfiguration;

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

    public AggregationControllerClientConfiguration getAggregationControllerClientConfiguration() {
        return aggregationControllerClientConfiguration;
    }

    public boolean isValid() {
        return clusterConfiguration != null
                && clientConfiguration != null
                && cryptoConfiguration != null
                && aggregatorConfiguration != null
                && aggregationControllerClientConfiguration != null;
    }
}
