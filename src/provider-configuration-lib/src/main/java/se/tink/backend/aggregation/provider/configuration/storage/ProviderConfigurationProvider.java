package se.tink.backend.aggregation.provider.configuration.storage;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.storage.converter.StorageProviderConfigurationConverter;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationDAO;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderStatusConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.repositories.ProviderStatusConfigurationRepository;
import se.tink.backend.core.ProviderStatuses;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProviderConfigurationProvider implements ProviderConfigurationDAO {
    private final Map<String, ProviderConfiguration> providerConfigurationByName;
    private final Map<String, List<String>> enabledProvidersOnCluster;
    private final Map<String, Map<String, ProviderConfiguration>> providerOverrideOnCluster;
    private final ProviderStatusConfigurationRepository providerStatusConfigurationRepository;

    private final static Logger logger = LoggerFactory.getLogger(ProviderConfigurationProvider.class);

    @Inject
    public ProviderConfigurationProvider(
            @Named("providerConfiguration") Map<String, ProviderConfiguration> providerConfigurationByName,
            @Named("enabledProvidersOnCluster") Map<String, List<String>> enabledProvidersOnCluster,
            @Named("providerOverrideOnCluster") Map<String, Map<String, ProviderConfiguration>> providerOverrideOnCluster,
            ProviderStatusConfigurationRepository providerStatusConfigurationRepository){
        this.providerConfigurationByName = providerConfigurationByName;
        this.enabledProvidersOnCluster = enabledProvidersOnCluster;
        this.providerOverrideOnCluster = providerOverrideOnCluster;
        this.providerStatusConfigurationRepository = providerStatusConfigurationRepository;
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAll(){
        return providerConfigurationByName.values().stream()
                .map(provider -> StorageProviderConfigurationConverter.translate(provider, getProviderStatus(provider)))
                .collect(Collectors.toList());
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByClusterId(String clusterId){
        return enabledProvidersOnCluster.get(clusterId).stream()
                .map(providerName -> getProviderConfigurationForCluster(clusterId, providerName))
                .map(provider -> StorageProviderConfigurationConverter.translate(provider, getProviderStatus(provider)))
                .collect(Collectors.toList());
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByMarket(String market) {
        return providerConfigurationByName.values().stream()
                .filter(providerConfiguration -> Objects.equals(market, providerConfiguration.getMarket()))
                .map(provider -> StorageProviderConfigurationConverter.translate(provider, getProviderStatus(provider)))
                .collect(Collectors.toList());
    }

    public se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration findByClusterIdAndProviderName(String clusterId, String providerName) {
        ProviderConfiguration providerConfiguration = getProviderConfigurationForCluster(clusterId, providerName);
        return StorageProviderConfigurationConverter.translate(providerConfiguration, getProviderStatus(providerConfiguration));
    }

    public se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration findByName(String providerName) {
        ProviderConfiguration providerConfiguration = providerConfigurationByName.get(providerName);
        return StorageProviderConfigurationConverter.translate(providerConfiguration, getProviderStatus(providerConfiguration));
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByClusterIdAndMarket(String clusterId, String market) {
        return findAllByClusterId(clusterId).stream()
                .filter(providerConfiguration -> Objects.equals(market, providerConfiguration.getMarket()))
                .collect(Collectors.toList());
    }

    private ProviderConfiguration getProviderConfigurationForCluster(String clusterId, String providerName){
        if (providerOverrideOnCluster.containsKey(clusterId) &&
                providerOverrideOnCluster.get(clusterId).containsKey(providerName)) {
            return providerOverrideOnCluster.get(clusterId).get(providerName);
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
