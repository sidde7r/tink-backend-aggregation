package se.tink.backend.aggregation.configurations;

import com.google.inject.Inject;
import se.tink.backend.aggregation.configurations.repositories.AggregatorConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.ClientConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.ClusterConfigurationsRepository;
import se.tink.backend.aggregation.configurations.repositories.CryptoConfigurationsRepository;

public class AggregationConfigurations implements ConfigurationsDao {

    private CryptoConfigurationsRepository cryptoConfigurationsRepository;
    private ClientConfigurationsRepository clientConfigurationsRepository;
    private AggregatorConfigurationsRepository aggregatorConfigurationsRepository;
    private ClusterConfigurationsRepository clusterConfigurationsRepository;

    @Inject
    public AggregationConfigurations(CryptoConfigurationsRepository cryptoConfigurationsRepository,
            ClientConfigurationsRepository clientConfigurationsRepository,
            AggregatorConfigurationsRepository aggregatorConfigurationsRepository,
            ClusterConfigurationsRepository clusterConfigurationsRepository) {
        this.cryptoConfigurationsRepository = cryptoConfigurationsRepository;
        this.clientConfigurationsRepository = clientConfigurationsRepository;
        this.aggregatorConfigurationsRepository = aggregatorConfigurationsRepository;
        this.clusterConfigurationsRepository = clusterConfigurationsRepository;
    }

}
