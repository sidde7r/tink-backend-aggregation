package se.tink.backend.aggregation.provider.configuration.http.converter;

import se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;

import java.util.List;
import java.util.stream.Collectors;

public class HttpProviderConfigurationConverter {
    public static ProviderConfigurationDTO translate(ProviderConfiguration providerConfiguration) {
        // TODO: implementation
        return null;
    }

    public static List<ProviderConfigurationDTO> translate(List<ProviderConfiguration> providerConfigurationList) {
        return providerConfigurationList.stream()
                .map(HttpProviderConfigurationConverter::translate)
                .collect(Collectors.toList());
    }
}
