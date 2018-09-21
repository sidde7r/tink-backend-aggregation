package se.tink.backend.aggregation.provider.configuration.storage.converter;

import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderStatusConfiguration;
import se.tink.backend.core.ProviderStatuses;

import java.util.Optional;

public class StorageProviderConfigurationConverter {
    public static se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration convert(
            ProviderConfiguration providerConfiguration, Optional<ProviderStatusConfiguration> providerStatusConfiguration) {
        se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration core =
                new se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration();
        core.setCapabilitiesSerialized(providerConfiguration.getCapabilitiesSerialized());
        core.setClassName(providerConfiguration.getClassName());
        core.setCredentialsType(providerConfiguration.getCredentialsType());
        core.setCurrency(providerConfiguration.getCurrency());
        core.setDisplayName(providerConfiguration.getDisplayName());
        core.setGroupDisplayName(providerConfiguration.getGroupDisplayName());
        core.setMarket(providerConfiguration.getMarket());
        core.setMultiFactor(providerConfiguration.isMultiFactor());
        core.setName(providerConfiguration.getName());
        core.setPasswordHelpText(providerConfiguration.getPasswordHelpText());
        core.setPayload(providerConfiguration.getPayload());
        core.setPopular(providerConfiguration.isPopular());
        core.setRefreshFrequency(providerConfiguration.getRefreshFrequency());
        core.setRefreshFrequencyFactor(providerConfiguration.getRefreshFrequencyFactor());
        core.setType(providerConfiguration.getType());
        core.setFields(providerConfiguration.getFields());
        core.setStatus(determineProviderStatus(providerConfiguration, providerStatusConfiguration));
        core.setTransactional(providerConfiguration.isTransactional());
        core.setDisplayDescription(providerConfiguration.getDisplayDescription());

        providerConfiguration.getRefreshSchedule().ifPresent(
                prs -> core.setRefreshSchedule(prs)
        );

        return core;
    }

    /**
     * determinProviderStatus takes the provider status in database if present
     * if not, takes the local status
     * @param providerConfiguration
     * @param providerStatusConfiguration
     * @return
     */
    private static ProviderStatuses determineProviderStatus(ProviderConfiguration providerConfiguration,
                                                     Optional<ProviderStatusConfiguration> providerStatusConfiguration){
        ProviderStatuses providerStatuses = providerConfiguration.getStatus();
        providerStatusConfiguration.ifPresent(psc -> providerConfiguration.setStatus(psc.getStatus()));

        return providerStatuses;
    }
}
