package se.tink.backend.aggregation.provider.configuration.http.converter;

import com.google.common.collect.Sets;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;
import se.tink.libraries.serialization.utils.SerializationUtils;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class HttpProviderConfigurationConverter {
    private static final Set<ProviderConfiguration.Capability> NON_OXFORD_CAPABILITIES = Sets.immutableEnumSet(
            ProviderConfiguration.Capability.MORTGAGE_AGGREGATION, ProviderConfiguration.Capability.TRANSFERS);

    private static final Predicate<ProviderConfiguration.Capability> NON_OXFORD_CAPABILITIES_FILTER =
            NON_OXFORD_CAPABILITIES::contains;

    public static ProviderConfigurationDTO convert(String clusterId, ProviderConfiguration providerConfiguration) {
        ProviderConfigurationDTO dto = new ProviderConfigurationDTO();
        dto.setCapabilitiesSerialized(handleCapabilities(clusterId, providerConfiguration));
        dto.setClassName(providerConfiguration.getClassName());
        dto.setCredentialsType(providerConfiguration.getCredentialsType());
        dto.setCurrency(providerConfiguration.getCurrency());
        dto.setDisplayName(providerConfiguration.getDisplayName());
        dto.setDisplayDescription(providerConfiguration.getDisplayDescription());
        dto.setFields(providerConfiguration.getFields());
        dto.setGroupDisplayName(providerConfiguration.getGroupDisplayName());
        dto.setMarket(providerConfiguration.getMarket());
        dto.setMultiFactor(providerConfiguration.isMultiFactor());
        dto.setName(providerConfiguration.getName());
        dto.setPasswordHelpText(providerConfiguration.getPasswordHelpText());
        dto.setPayload(providerConfiguration.getPayload());
        dto.setPopular(providerConfiguration.isPopular());
        dto.setRefreshFrequency(providerConfiguration.getRefreshFrequency());
        dto.setRefreshFrequencyFactor(providerConfiguration.getRefreshFrequencyFactor());
        dto.setStatus(providerConfiguration.getStatus());
        dto.setTransactional(providerConfiguration.isTransactional());
        dto.setTutorialUrl(providerConfiguration.getTutorialUrl());
        dto.setType(providerConfiguration.getType());
        dto.setRefreshSchedule(providerConfiguration.getRefreshSchedule().orElse(null));
        dto.setStatus(providerConfiguration.getStatus());
        dto.setSupplementalFields(providerConfiguration.getSupplementalFields());
        return dto;
    }

    public static List<ProviderConfigurationDTO> convert(
            List<ProviderConfiguration> providerConfigurationList, String clusterId) {
        return providerConfigurationList.stream()
                .map(providerConfiguration -> convert(clusterId, providerConfiguration))
                .collect(Collectors.toList());
    }

    private static String handleCapabilities(String clusterId, ProviderConfiguration providerConfiguration) {
        // TODO: This can be removed when all clusters are running the following version of respective service:
        // Aggregation Controller: 201812031439-3488b872
        // Main: 201812030837-6fa50f81
        // System: 201812030837-6fa50f81
        if (!clusterId.contains("oxford")) {
            Set<ProviderConfiguration.Capability> capabilities = providerConfiguration.getCapabilities().stream()
                    .filter(NON_OXFORD_CAPABILITIES_FILTER)
                    .collect(Collectors.toSet());
            return SerializationUtils.serializeToString(capabilities);
        }

        return providerConfiguration.getCapabilitiesSerialized();
    }
}
