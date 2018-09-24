package se.tink.backend.aggregation.provider.configuration.http.converter;

import se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;

import java.util.List;
import java.util.stream.Collectors;

public class HttpProviderConfigurationConverter {
    public static ProviderConfigurationDTO convert(ProviderConfiguration providerConfiguration) {
        ProviderConfigurationDTO dto = new ProviderConfigurationDTO();
        dto.setCapabilitiesSerialized(providerConfiguration.getCapabilitiesSerialized());
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
        dto.setType(providerConfiguration.getType());
        dto.setRefreshSchedule(providerConfiguration.getRefreshSchedule().orElse(null));
        dto.setStatus(providerConfiguration.getStatus());
        return dto;
    }

    public static List<ProviderConfigurationDTO> convert(List<ProviderConfiguration> providerConfigurationList) {
        return providerConfigurationList.stream()
                .map(HttpProviderConfigurationConverter::convert)
                .collect(Collectors.toList());
    }
}
