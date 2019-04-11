package se.tink.backend.aggregation.provider.configuration.http.converter;

import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationCore;
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class HttpProviderConfigurationConverter {
    private static final Set<ProviderConfigurationCore.Capability> NON_OXFORD_CAPABILITIES =
            Sets.immutableEnumSet(
                    ProviderConfigurationCore.Capability.MORTGAGE_AGGREGATION,
                    ProviderConfigurationCore.Capability.TRANSFERS);

    private static final Predicate<ProviderConfigurationCore.Capability>
            NON_OXFORD_CAPABILITIES_FILTER = NON_OXFORD_CAPABILITIES::contains;

    public static ProviderConfigurationDTO convert(
            String clusterId, ProviderConfigurationCore providerConfigurationCore) {

        ProviderConfigurationDTO dto = new ProviderConfigurationDTO();

        dto.setAccessType(convertAccessType(providerConfigurationCore.getAccessType()));
        dto.setCapabilitiesSerialized(handleCapabilities(clusterId, providerConfigurationCore));
        dto.setClassName(providerConfigurationCore.getClassName());
        dto.setCredentialsType(providerConfigurationCore.getCredentialsType());
        dto.setCurrency(providerConfigurationCore.getCurrency());
        dto.setDisplayName(providerConfigurationCore.getDisplayName());
        dto.setDisplayDescription(providerConfigurationCore.getDisplayDescription());
        dto.setFields(providerConfigurationCore.getFields());
        dto.setFinancialInstituteId(providerConfigurationCore.getFinancialInstituteId());
        dto.setFinancialInstituteName(providerConfigurationCore.getFinancialInstituteName());
        dto.setGroupDisplayName(providerConfigurationCore.getGroupDisplayName());
        dto.setMarket(providerConfigurationCore.getMarket());
        dto.setMultiFactor(providerConfigurationCore.isMultiFactor());
        dto.setName(providerConfigurationCore.getName());
        dto.setPasswordHelpText(providerConfigurationCore.getPasswordHelpText());
        dto.setPayload(providerConfigurationCore.getPayload());
        dto.setPopular(providerConfigurationCore.isPopular());
        dto.setRefreshFrequency(providerConfigurationCore.getRefreshFrequency());
        dto.setRefreshFrequencyFactor(providerConfigurationCore.getRefreshFrequencyFactor());
        dto.setStatus(providerConfigurationCore.getStatus());
        dto.setTransactional(providerConfigurationCore.isTransactional());
        dto.setTutorialUrl(providerConfigurationCore.getTutorialUrl());
        dto.setType(providerConfigurationCore.getType());
        dto.setRefreshSchedule(providerConfigurationCore.getRefreshSchedule().orElse(null));
        dto.setStatus(providerConfigurationCore.getStatus());
        dto.setSupplementalFields(providerConfigurationCore.getSupplementalFields());
        return dto;
    }

    public static List<ProviderConfigurationDTO> convert(
            List<ProviderConfigurationCore> providerConfigurationCoreList, String clusterId) {
        return providerConfigurationCoreList.stream()
                .map(providerConfiguration -> convert(clusterId, providerConfiguration))
                .collect(Collectors.toList());
    }

    private static String handleCapabilities(
            String clusterId, ProviderConfigurationCore providerConfigurationCore) {
        // TODO: This can be removed when all clusters are running the following version of
        // respective service:
        // Aggregation Controller: 201812031439-3488b872
        // Main: 201812030837-6fa50f81
        // System: 201812030837-6fa50f81
        if (!clusterId.contains("oxford")) {
            Set<ProviderConfigurationCore.Capability> capabilities =
                    providerConfigurationCore.getCapabilities().stream()
                            .filter(NON_OXFORD_CAPABILITIES_FILTER)
                            .collect(Collectors.toSet());
            return SerializationUtils.serializeToString(capabilities);
        }

        return providerConfigurationCore.getCapabilitiesSerialized();
    }

    private static ProviderConfigurationDTO.AccessType convertAccessType(
            ProviderConfigurationCore.AccessType accessType) {
        if (accessType == null) {
            return null;
        }

        return ProviderConfigurationDTO.AccessType.valueOf(accessType.name());
    }
}
