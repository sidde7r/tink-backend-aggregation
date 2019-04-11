package se.tink.backend.aggregation.storage.database.daos;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;
import se.tink.backend.aggregation.wrappers.CryptoWrapper;

public class CryptoConfigurationDao {
    private Map<String, List<CryptoConfiguration>> cryptoConfigurationsByClientName;

    @Inject
    CryptoConfigurationDao(CryptoConfigurationsRepository cryptoConfigurationsRepository) {
        List<CryptoConfiguration> allCryptoConfigurations =
                cryptoConfigurationsRepository.findAll();

        if (Objects.isNull(allCryptoConfigurations) || allCryptoConfigurations.isEmpty()) {
            throw new IllegalStateException();
        }

        this.cryptoConfigurationsByClientName =
                allCryptoConfigurations.stream()
                        .collect(
                                Collectors.groupingBy(
                                        configuration ->
                                                configuration
                                                        .getCryptoConfigurationId()
                                                        .getClientName()));
    }

    public CryptoWrapper getCryptoWrapperOfClientName(String clientName) {
        List<CryptoConfiguration> cryptoConfigurations =
                cryptoConfigurationsByClientName.getOrDefault(clientName, Collections.emptyList());

        Preconditions.checkArgument(
                !cryptoConfigurations.isEmpty(),
                "Could not find cryptoConfigurations for clientName %s.",
                clientName);
        return CryptoWrapper.of(ImmutableList.copyOf(cryptoConfigurations));
    }
}
