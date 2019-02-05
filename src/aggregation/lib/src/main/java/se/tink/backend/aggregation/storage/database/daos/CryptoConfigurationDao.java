package se.tink.backend.aggregation.storage.database.daos;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.List;
import javax.inject.Inject;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;

public class CryptoConfigurationDao {
    private CryptoConfigurationsRepository cryptoConfigurationsRepository;

    @Inject
    CryptoConfigurationDao(CryptoConfigurationsRepository cryptoConfigurationsRepository) {
        this.cryptoConfigurationsRepository = cryptoConfigurationsRepository;
    }

    public CryptoWrapper getCryptoWrapperOfClientName(String clientName) {
        List<CryptoConfiguration> cryptoConfigurations = cryptoConfigurationsRepository
                .findByCryptoConfigurationIdClientName(clientName);

        Preconditions.checkNotNull(cryptoConfigurations,
                "Could not find cryptoConfiguration for clientName %s.", clientName);
        Preconditions.checkArgument(!cryptoConfigurations.isEmpty(),
                "Could not find cryptoConfigurations for clientName %s.", clientName);
        return CryptoWrapper.of(ImmutableList.copyOf(cryptoConfigurations));
    }
}
