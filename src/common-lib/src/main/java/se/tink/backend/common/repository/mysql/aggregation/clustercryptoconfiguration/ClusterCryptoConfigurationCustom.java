package se.tink.backend.common.repository.mysql.aggregation.clustercryptoconfiguration;

import java.util.List;
import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.CryptoId;

public interface ClusterCryptoConfigurationCustom {
    List<ClusterCryptoConfiguration> findByCryptoIdClusterId(String clusterId);
    ClusterCryptoConfiguration findByCryptoId(CryptoId cryptoId);
}
