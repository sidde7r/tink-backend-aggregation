package se.tink.backend.aggregation.storage.database.repositories;

import java.util.List;
import se.tink.backend.core.ClusterCryptoConfiguration;
import se.tink.backend.core.CryptoId;

public interface ClusterCryptoConfigurationCustom {
    List<ClusterCryptoConfiguration> findByCryptoIdClusterId(String clusterId);
    ClusterCryptoConfiguration findByCryptoId(CryptoId cryptoId);
}
