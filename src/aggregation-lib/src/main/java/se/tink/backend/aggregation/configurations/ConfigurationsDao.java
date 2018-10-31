package se.tink.backend.aggregation.configurations;

import java.util.List;
import se.tink.backend.aggregation.configurations.models.AggregatorConfiguration;
import se.tink.backend.aggregation.configurations.models.ClientConfiguration;
import se.tink.backend.aggregation.configurations.models.ClusterConfiguration;
import se.tink.backend.aggregation.configurations.models.CryptoConfiguration;

public interface ConfigurationsDao {
    List<CryptoConfiguration> getCryptoConfigurations();

    List<ClientConfiguration> getClientConfigurations();

    List<AggregatorConfiguration> getAggregatorConfigurations();

    List<ClusterConfiguration> getClusterConfigurations();

    CryptoConfiguration findCryptoConfiguration(int keyId, String cryptoId);

    ClientConfiguration findClientConfigurations(String clientId);

    AggregatorConfiguration findAggregatorConfigurations(String aggregatorId);

    ClusterConfiguration findClusterConfigurations(String clusterId);

    CryptoConfiguration findOneByKeyId(int keyId);
}
