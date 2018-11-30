package se.tink.backend.aggregation.provider.configuration.storage.converter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;

public class StorageProviderConfigurationConverter {
    public static se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration convert(
            ProviderConfiguration providerConfiguration) {
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
        core.setStatus(providerConfiguration.getStatus());
        core.setTransactional(providerConfiguration.isTransactional());
        core.setDisplayDescription(providerConfiguration.getDisplayDescription());

        providerConfiguration.getRefreshSchedule().ifPresent(
                prs -> core.setRefreshSchedule(prs)
        );

        return core;
    }

    public static List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> convert(
            Collection<ProviderConfiguration> providerConfiguration) {
        return providerConfiguration.stream().map(StorageProviderConfigurationConverter::convert).collect(
                Collectors.toList());
    }
}
