package se.tink.backend.aggregation.provider.configuration.storage.converter;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationCore;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationStorage;
import se.tink.libraries.provider.enums.ProviderStatuses;

public class StorageProviderConfigurationConverter {

    public static List<ProviderConfigurationCore> convert(
            Collection<ProviderConfigurationStorage> providerConfigurationStorage,
            Map<String, ProviderStatuses> providerStatusesMap) {
        return providerConfigurationStorage.stream()
                .map(
                        p ->
                                StorageProviderConfigurationConverter.convert(
                                        p, getProviderStatusIfExists(providerStatusesMap, p)))
                .collect(Collectors.toList());
    }

    private static Optional<ProviderStatuses> getProviderStatusIfExists(
            Map<String, ProviderStatuses> providerStatusesMap,
            ProviderConfigurationStorage providerConfigurationStorage) {
        return Optional.ofNullable(providerStatusesMap.get(providerConfigurationStorage.getName()));
    }

    public static ProviderConfigurationCore convert(
            ProviderConfigurationStorage providerConfigurationStorage,
            Optional<ProviderStatuses> providerStatus) {

        ProviderConfigurationCore core = new ProviderConfigurationCore();

        core.setAccessType(convertAccessType(providerConfigurationStorage.getAccessType()));
        core.setCapabilitiesSerialized(providerConfigurationStorage.getCapabilitiesSerialized());
        core.setClassName(providerConfigurationStorage.getClassName());
        core.setCredentialsType(providerConfigurationStorage.getCredentialsType());
        core.setCurrency(providerConfigurationStorage.getCurrency());
        core.setDisplayName(providerConfigurationStorage.getDisplayName());
        core.setFinancialInstituteId(providerConfigurationStorage.getFinancialInstituteId());
        core.setFinancialInstituteName(providerConfigurationStorage.getFinancialInstituteName());
        core.setGroupDisplayName(providerConfigurationStorage.getGroupDisplayName());
        core.setMarket(providerConfigurationStorage.getMarket());
        core.setMultiFactor(providerConfigurationStorage.isMultiFactor());
        core.setName(providerConfigurationStorage.getName());
        core.setPasswordHelpText(providerConfigurationStorage.getPasswordHelpText());
        core.setPayload(providerConfigurationStorage.getPayload());
        core.setPopular(providerConfigurationStorage.isPopular());
        core.setRefreshFrequency(providerConfigurationStorage.getRefreshFrequency());
        core.setRefreshFrequencyFactor(providerConfigurationStorage.getRefreshFrequencyFactor());
        core.setType(providerConfigurationStorage.getType());
        core.setFields(providerConfigurationStorage.getFields());
        core.setTransactional(providerConfigurationStorage.isTransactional());
        core.setDisplayDescription(providerConfigurationStorage.getDisplayDescription());
        core.setSupplementalFields(providerConfigurationStorage.getSupplementalFields());
        core.setTutorialUrl(providerConfigurationStorage.getTutorialUrl());

        if (providerStatus.isPresent()) {
            core.setStatus(providerStatus.get());
        } else {
            core.setStatus(providerConfigurationStorage.getStatus());
        }
        providerConfigurationStorage
                .getRefreshSchedule()
                .ifPresent(prs -> core.setRefreshSchedule(prs));

        return core;
    }

    private static ProviderConfigurationCore.AccessType convertAccessType(
            ProviderConfigurationStorage.AccessType accessType) {
        if (accessType == null) {
            return null;
        }

        return ProviderConfigurationCore.AccessType.valueOf(accessType.name());
    }
}
