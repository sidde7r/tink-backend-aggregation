package se.tink.backend.aggregation.provider.configuration.converter;

import se.tink.backend.aggregation.provider.configuration.repositories.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.repositories.ProviderStatusConfiguration;
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;
import se.tink.backend.core.ProviderStatuses;

import java.util.Optional;

public class ProviderConfigurationDTOConverter {
    
    private ProviderConfigurationDTO dto = new ProviderConfigurationDTO();
    
    public ProviderConfigurationDTOConverter(ProviderConfiguration providerConfiguration){
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

    }
    public ProviderConfigurationDTO convert(){
        return dto;
    }
    public ProviderConfigurationDTO convert(Optional<ProviderStatusConfiguration> providerStatusConfiguration){
        providerStatusConfiguration.ifPresent(providerStatusConfiguration1 -> dto.setStatus(providerStatusConfiguration1.getStatus()));
        return dto;
    }
}
