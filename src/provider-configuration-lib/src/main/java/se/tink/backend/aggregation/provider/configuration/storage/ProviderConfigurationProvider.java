package se.tink.backend.aggregation.provider.configuration.storage;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import se.tink.backend.aggregation.provider.configuration.storage.converter.StorageProviderConfigurationConverter;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationDAO;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderStatusConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.repositories.ProviderStatusConfigurationRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProviderConfigurationProvider implements ProviderConfigurationDAO {
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

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAll(){
        return providerConfigurationByName.values().stream()
                .map(StorageProviderConfigurationConverter::translate)
//                .map(ProviderConfigurationDAO:) TODO: set status
                .collect(Collectors.toList());
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByClusterId(String clusterId){
        return providerEnabledByCluster.get(clusterId).stream()
                .map(providerName -> getProviderConfigurationForCluster(clusterId, providerName))
                .map(StorageProviderConfigurationConverter::translate)
//                .map(this::setProviderConfigurationStatus) TODO: set status
                .collect(Collectors.toList());
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByMarket(String market) {
        return providerConfigurationByName.values().stream()
                .filter(providerConfiguration -> Objects.equals(market, providerConfiguration.getMarket()))
                .map(StorageProviderConfigurationConverter::translate)
//                .map(this::setProviderConfigurationStatus) TODO: set status
                .collect(Collectors.toList());
    }

    public se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration findByClusterIdAndProviderName(String clusterId, String providerName) {
        return StorageProviderConfigurationConverter.translate(setProviderConfigurationStatus(getProviderConfigurationForCluster(clusterId, providerName)));
    }

    public se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration findByName(String providerName) {
        return StorageProviderConfigurationConverter.translate(setProviderConfigurationStatus(providerConfigurationByName.get(providerName)));
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByClusterIdAndMarket(String clusterId, String market) {
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

    private Optional<ProviderStatusConfiguration> getProviderStatus(ProviderConfiguration providerConfiguration){
        return providerStatusConfigurationRepository.getProviderStatusConfiguration(providerConfiguration.getName());
    }

    @Override
    public void updateStatus(String providerName, ProviderStatuses providerStatus) {
        ProviderStatusConfiguration providerStatusConfiguration = providerStatusConfigurationRepository.getOne(providerName);
        if (providerStatusConfiguration != null) {
            providerStatusConfiguration.setStatus(providerStatus);
            providerStatusConfigurationRepository.save(providerStatusConfiguration);
            logger.info("Provider status updated");
        } else {
            logger.warn(String.format("Provider '%s' could not be found.", providerName));
        }
    }
}
