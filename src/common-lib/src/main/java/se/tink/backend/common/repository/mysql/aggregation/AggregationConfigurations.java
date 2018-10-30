package se.tink.backend.common.repository.mysql.aggregation;


import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.common.config.ConfigurationsDao;
import se.tink.backend.common.repository.mysql.aggregation.aggregatorconfigurations.AggregatorConfigurationsRepository;
import se.tink.backend.common.repository.mysql.aggregation.clientconfigurations.ClientConfigurationsRepository;
import se.tink.backend.common.repository.mysql.aggregation.clusterconfigurations.ClusterConfigurationsRepository;
import se.tink.backend.common.repository.mysql.aggregation.cryptoconfigurations.CryptoConfigurationsRepository;
import se.tink.backend.core.AggregatorConfiguration;
import se.tink.backend.core.ClientConfiguration;
import se.tink.backend.core.ClusterConfiguration;
import se.tink.backend.core.CryptoConfiguration;

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
        return cryptoConfigurationsRepository.findAll()
                .stream()
                .filter(e -> e.getKeyId() == keyId && e.getCryptoId().equalsIgnoreCase(cryptoId))
                .findFirst()
                .get();
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
}
