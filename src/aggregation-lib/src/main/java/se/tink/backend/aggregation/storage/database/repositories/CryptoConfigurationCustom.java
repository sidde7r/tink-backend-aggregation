package se.tink.backend.aggregation.storage.database.repositories;

import java.util.List;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfigurationId;

public interface CryptoConfigurationCustom {
    List<CryptoConfiguration> findByCryptoConfigurationIdClientName(String clientName);
    CryptoConfiguration findByCryptoConfigurationId(CryptoConfigurationId cryptoId);
}
