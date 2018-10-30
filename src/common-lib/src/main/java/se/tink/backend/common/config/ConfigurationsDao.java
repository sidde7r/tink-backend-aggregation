package se.tink.backend.common.config;

import java.util.List;
import se.tink.backend.core.AggregatorConfigurations;
import se.tink.backend.core.ClientConfigurations;
import se.tink.backend.core.ClusterConfigurations;
import se.tink.backend.core.CryptoConfigurations;

public interface ConfigurationsDao {
    List<CryptoConfigurations> getCryptoConfigurations();

    List<ClientConfigurations> getClientConfigurations();

    List<AggregatorConfigurations> getAggregatorConfigurations();

    List<ClusterConfigurations> getClusterConfigurations();

    CryptoConfigurations findCryptoConfiguration(int keyId);

    ClientConfigurations findClientConfigurations(String clientId);

    AggregatorConfigurations findAggregatorConfigurations(String aggregatorId);

    ClusterConfigurations findClusterConfigurations(String clusterId);
}
