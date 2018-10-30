package se.tink.backend.common.repository.mysql.aggregation;


import com.google.inject.Inject;
import java.util.List;
import se.tink.backend.common.config.ConfigurationsDao;
import se.tink.backend.common.repository.mysql.aggregation.aggregatorconfigurations.AggregatorConfigurationsRepository;
import se.tink.backend.common.repository.mysql.aggregation.clientconfigurations.ClientConfigurationsRepository;
import se.tink.backend.common.repository.mysql.aggregation.clusterconfigurations.ClusterConfigurationsRepository;
import se.tink.backend.common.repository.mysql.aggregation.cryptoconfigurations.CryptoConfigurationsRepository;
import se.tink.backend.core.AggregatorConfigurations;
import se.tink.backend.core.ClientConfigurations;
import se.tink.backend.core.ClusterConfigurations;
import se.tink.backend.core.CryptoConfigurations;

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
    public List<CryptoConfigurations> getCryptoConfigurations() {
        return cryptoConfigurationsRepository.findAll();
    }

    @Override
    public List<ClientConfigurations> getClientConfigurations() {
        return clientConfigurationsRepository.findAll();
    }

    @Override
    public List<AggregatorConfigurations> getAggregatorConfigurations() {
        return aggregatorConfigurationsRepository.findAll();
    }

    @Override
    public List<ClusterConfigurations> getClusterConfigurations() {
        return clusterConfigurationsRepository.findAll();
    }

    @Override
    public CryptoConfigurations findCryptoConfiguration(int keyId) {
        return cryptoConfigurationsRepository.findOne(""+keyId);
    }

    @Override
    public ClientConfigurations findClientConfigurations(String clientId) {
        return clientConfigurationsRepository.findOne(clientId);
    }

    @Override
    public AggregatorConfigurations findAggregatorConfigurations(String aggregatorId) {
        return aggregatorConfigurationsRepository.findOne(aggregatorId);
    }

    @Override
    public ClusterConfigurations findClusterConfigurations(String clusterId) {
        return clusterConfigurationsRepository.findOne(clusterId);
    }
}
