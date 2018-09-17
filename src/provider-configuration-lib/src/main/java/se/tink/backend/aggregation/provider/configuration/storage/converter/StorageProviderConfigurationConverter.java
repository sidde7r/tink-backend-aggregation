package se.tink.backend.aggregation.provider.configuration.storage.converter;

import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderStatusConfiguration;

import java.util.Optional;

public class StorageProviderConfigurationConverter {
    public static se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration translate(
            ProviderConfiguration providerConfiguration, Optional<ProviderStatusConfiguration> providerStatusConfiguration) {
        se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration core =
                new se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration();
        core.setCapabilitiesSerialized(providerConfiguration.getCapabilitiesSerialized());
        core.setClassName(providerConfiguration.getClassName());
        core.setCredentialsType(providerConfiguration.getCredentialsType());
        core.setCurrency(providerConfiguration.getCurrency());
        core.setDisplayName(providerConfiguration.getDisplayName());
//      FIXME  core.setFieldsSerialized
        core.setGroupDisplayName(providerConfiguration.getGroupDisplayName());
        core.setMarket(providerConfiguration.getMarket());
        core.setMultiFactor(providerConfiguration.isMultiFactor());
        core.setName(providerConfiguration.getName());
        core.setPasswordHelpText(providerConfiguration.getPasswordHelpText());
        core.setPayload(providerConfiguration.getPayload());
        core.setPopular(providerConfiguration.isPopular());
        core.setRefreshFrequency(providerConfiguration.getRefreshFrequency());
        core.setRefreshFrequencyFactor(providerConfiguration.getRefreshFrequencyFactor());

        // fixme: never return null
        providerStatusConfiguration.ifPresent(psc -> core.setStatus(psc.getStatus()));

        core.setTransactional(providerConfiguration.isTransactional());
        core.setDisplayDescription(providerConfiguration.getDisplayDescription());

        providerConfiguration.getRefreshSchedule().ifPresent(
                prs -> core.setRefreshSchedule(prs)
        );

        return core;
    }
}
