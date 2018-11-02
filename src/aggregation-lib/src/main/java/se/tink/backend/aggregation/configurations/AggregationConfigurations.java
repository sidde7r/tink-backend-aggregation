package se.tink.backend.aggregation.configurations;


import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.aggregation.configurations.models.AggregatorConfiguration;
import se.tink.backend.aggregation.configurations.models.ClientConfiguration;
import se.tink.backend.aggregation.configurations.models.ClusterConfiguration;
import se.tink.backend.aggregation.configurations.models.CryptoConfiguration;
import se.tink.backend.aggregation.configurations.models.CryptoConfigurationId;
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

    @Override
    public List<CryptoConfiguration> getCryptoConfigurations() {
        return cryptoConfigurationsRepository.findAll();
    }

    @Override
    public List<ClientConfiguration> getClientConfigurations() {
        return clientConfigurationsRepository.findAll();
    }

    @Override
    public List<AggregatorConfiguration> getAggregatorConfigurations() {
        return aggregatorConfigurationsRepository.findAll();
    }

    @Override
    public List<ClusterConfiguration> getClusterConfigurations() {
        return clusterConfigurationsRepository.findAll();
    }

    @Override
    public CryptoConfiguration findCryptoConfiguration(int keyId, String cryptoId) {
        return cryptoConfigurationsRepository.findOne(new CryptoConfigurationId(keyId,cryptoId));
    }

    @Override
    public ClientConfiguration findClientConfigurations(String clientId) {
        return clientConfigurationsRepository.findOne(clientId);
    }

    @Override
    public AggregatorConfiguration findAggregatorConfigurations(String aggregatorId) {
        return aggregatorConfigurationsRepository.findOne(aggregatorId);
    }

    @Override
    public ClusterConfiguration findClusterConfigurations(String clusterId) {
        return clusterConfigurationsRepository.findOne(clusterId);
    }

    @Override
    public List<CryptoConfiguration> findOneByKeyId(int keyId) {
        return cryptoConfigurationsRepository.findBycryptoConfigurationIdKeyId(keyId);
    }
}
