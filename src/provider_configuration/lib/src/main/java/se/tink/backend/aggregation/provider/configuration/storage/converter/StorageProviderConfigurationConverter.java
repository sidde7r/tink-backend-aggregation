package se.tink.backend.aggregation.provider.configuration.storage.converter;

import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import se.tink.libraries.provider.enums.ProviderStatuses;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class StorageProviderConfigurationConverter {

    public static List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> convert(
            Collection<ProviderConfiguration> providerConfiguration,
            Map<String, ProviderStatuses> providerStatusesMap) {
        return providerConfiguration
                .stream()
                .map(p -> StorageProviderConfigurationConverter
                        .convert(p, getProviderStatusIfExists(providerStatusesMap, p)))
                .collect(Collectors.toList());
    }

    private static Optional<ProviderStatuses> getProviderStatusIfExists(
            Map<String, ProviderStatuses> providerStatusesMap,
            ProviderConfiguration providerConfiguration) {
        return Optional.ofNullable(providerStatusesMap.get(providerConfiguration.getName()));
    }

    public static se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration convert(
            ProviderConfiguration providerConfiguration, Optional<ProviderStatuses> providerStatus) {

        se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration core =
                new se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration();

        core.setAccessType(
                convertAccessType(providerConfiguration.getAccessType())
        );
        core.setCapabilitiesSerialized(providerConfiguration.getCapabilitiesSerialized());
        core.setClassName(providerConfiguration.getClassName());
        core.setCredentialsType(providerConfiguration.getCredentialsType());
        core.setCurrency(providerConfiguration.getCurrency());
        core.setDisplayName(providerConfiguration.getDisplayName());
        core.setFinancialInstituteId(providerConfiguration.getFinancialInstituteId());
        core.setFinancialInstituteName(providerConfiguration.getFinancialInstituteName());
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
        core.setTransactional(providerConfiguration.isTransactional());
        core.setDisplayDescription(providerConfiguration.getDisplayDescription());
        core.setSupplementalFields(providerConfiguration.getSupplementalFields());
        core.setTutorialUrl(providerConfiguration.getTutorialUrl());

        if (providerStatus.isPresent()) {
            core.setStatus(providerStatus.get());
        } else {
            core.setStatus(providerConfiguration.getStatus());
        }
        providerConfiguration.getRefreshSchedule().ifPresent(
                prs -> core.setRefreshSchedule(prs)
        );

        return core;

    }

    private static se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration.AccessType convertAccessType(ProviderConfiguration.AccessType accessType) {
        if (accessType == null) {
            return null;
        }

        return se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration.AccessType.valueOf(
                accessType.name()
        );
    }
}
