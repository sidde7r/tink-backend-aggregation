package se.tink.backend.aggregation.provider.configuration.controllers;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.provider.configuration.converter.ProviderConfigurationDTOConverter;
import se.tink.backend.aggregation.provider.configuration.repositories.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.repositories.ProviderStatusConfiguration;
import se.tink.backend.aggregation.provider.configuration.repositories.mysql.ProviderStatusConfigurationRepository;
import se.tink.backend.aggregation.provider.configuration.rpc.ProviderConfigurationDTO;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProviderConfigurationProvider {
    private final Map<String, ProviderConfiguration> providerConfigurationByName;
    private final Map<String, List<String>> providerEnabledByCluster;
    private final Map<String, Map<String, ProviderConfiguration>> providerConfigurationByCluster;
    private final ProviderStatusConfigurationRepository providerStatusConfigurationRepository;

    @Inject
    public ProviderConfigurationProvider(
            @Named("providerConfiguration") Map<String, ProviderConfiguration> providerConfigurationByName,
            @Named("clusterProviderList") Map<String, List<String>> providerEnabledByCluster,
            @Named("clusterSpecificProviderConfiguration") Map<String, Map<String, ProviderConfiguration>> providerConfigurationByCluster,
            ProviderStatusConfigurationRepository providerStatusConfigurationRepository){
        this.providerConfigurationByName = providerConfigurationByName;
        this.providerEnabledByCluster = providerEnabledByCluster;
        this.providerConfigurationByCluster = providerConfigurationByCluster;
        this.providerStatusConfigurationRepository = providerStatusConfigurationRepository;
    }

    public List<ProviderConfigurationDTO> findAll(){
        return providerConfigurationByName.values().stream()
                .map(this::setProviderConfigurationStatus)
                .collect(Collectors.toList());
    }

    public List<ProviderConfigurationDTO> findAllByClusterId(String clusterId){
        return providerEnabledByCluster.get(clusterId).stream()
                .map(providerName -> getProviderConfigurationForCluster(clusterId, providerName))
                .map(this::setProviderConfigurationStatus)
                .collect(Collectors.toList());
    }

    public List<ProviderConfigurationDTO> findAllByMarket(String market) {
        return providerConfigurationByName.values().stream()
                .filter(providerConfiguration -> Objects.equals(market, providerConfiguration.getMarket()))
                .map(this::setProviderConfigurationStatus)
                .collect(Collectors.toList());
    }

    public ProviderConfigurationDTO findByClusterIdAndProviderName(String clusterId, String providerName) {
        return setProviderConfigurationStatus(getProviderConfigurationForCluster(clusterId, providerName));
    }

    public ProviderConfigurationDTO findByName(String providerName) {
        return setProviderConfigurationStatus(providerConfigurationByName.get(providerName));
    }

    public List<ProviderConfigurationDTO> findAllByClusterIdAndMarket(String clusterId, String market) {
        return findAllByClusterId(clusterId).stream()
                .filter(providerConfiguration -> Objects.equals(market, providerConfiguration.getMarket()))
                .collect(Collectors.toList());
    }

    private ProviderConfiguration getProviderConfigurationForCluster(String clusterId, String providerName){
        if (providerConfigurationByCluster.containsKey(clusterId) &&
                providerConfigurationByCluster.get(clusterId).containsKey(providerName)) {
            return providerConfigurationByCluster.get(clusterId).get(providerName);
        } else{
            return providerConfigurationByName.get(providerName);
        }
    }

    private ProviderConfigurationDTO setProviderConfigurationStatus(ProviderConfiguration providerConfiguration){
        Optional<ProviderStatusConfiguration> statuses = providerStatusConfigurationRepository.getProviderStatusConfiguration(providerConfiguration.getName());
        return new ProviderConfigurationDTOConverter(providerConfiguration).convert(statuses);
    }
}
