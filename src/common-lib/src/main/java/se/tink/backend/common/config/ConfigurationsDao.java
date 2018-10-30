package se.tink.backend.common.config;

import java.util.List;
import se.tink.backend.core.AggregatorConfiguration;
import se.tink.backend.core.ClientConfiguration;
import se.tink.backend.core.ClusterConfiguration;
import se.tink.backend.core.CryptoConfiguration;

public interface ConfigurationsDao {
    List<CryptoConfiguration> getCryptoConfigurations();

    List<ClientConfiguration> getClientConfigurations();

    List<AggregatorConfiguration> getAggregatorConfigurations();

    List<ClusterConfiguration> getClusterConfigurations();

    CryptoConfiguration findCryptoConfiguration(int keyId, String cryptoId);

    ClientConfiguration findClientConfigurations(String clientId);

    AggregatorConfiguration findAggregatorConfigurations(String aggregatorId);

    ClusterConfiguration findClusterConfigurations(String clusterId);
}
