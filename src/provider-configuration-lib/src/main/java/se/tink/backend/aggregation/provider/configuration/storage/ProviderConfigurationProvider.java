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
import java.util.Set;
import java.util.stream.Collectors;

public class ProviderConfigurationProvider implements ProviderConfigurationDAO {
    private final Map<String, ProviderConfiguration> providerConfigurationByName;
    private final Map<String, Set<String>> enabledProvidersOnCluster;
    private final Map<String, Map<String, ProviderConfiguration>> providerOverrideOnCluster;
    private final ProviderStatusConfigurationRepository providerStatusConfigurationRepository;

    private final static Logger log = LoggerFactory.getLogger(ProviderConfigurationProvider.class);

    @Inject
    public ProviderConfigurationProvider(
            @Named("providerConfiguration") Map<String, ProviderConfiguration> providerConfigurationByName,
            @Named("enabledProvidersOnCluster") Map<String, Set<String>> enabledProvidersOnCluster,
            @Named("providerOverrideOnCluster") Map<String, Map<String, ProviderConfiguration>> providerOverrideOnCluster,
            ProviderStatusConfigurationRepository providerStatusConfigurationRepository){
        this.providerConfigurationByName = providerConfigurationByName;
        this.enabledProvidersOnCluster = enabledProvidersOnCluster;
        this.providerOverrideOnCluster = providerOverrideOnCluster;
        this.providerStatusConfigurationRepository = providerStatusConfigurationRepository;
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAll(){
        return providerConfigurationByName.values().stream()
                .map(provider -> StorageProviderConfigurationConverter.convert(provider, getProviderStatus(provider)))
                .collect(Collectors.toList());
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByClusterId(String clusterId){
        return enabledProvidersOnCluster.get(clusterId).stream()
                .map(providerName -> getProviderConfigurationForCluster(clusterId, providerName))
                .map(provider -> StorageProviderConfigurationConverter.convert(provider, getProviderStatus(provider)))
                .collect(Collectors.toList());
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByMarket(String market) {
        return providerConfigurationByName.values().stream()
                .filter(providerConfiguration -> Objects.equals(market, providerConfiguration.getMarket()))
                .map(provider -> StorageProviderConfigurationConverter.convert(provider, getProviderStatus(provider)))
                .collect(Collectors.toList());
    }

    public se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration findByClusterIdAndProviderName(String clusterId, String providerName) {
        ProviderConfiguration providerConfiguration = getProviderConfigurationForCluster(clusterId, providerName);
        return StorageProviderConfigurationConverter.convert(providerConfiguration, getProviderStatus(providerConfiguration));
    }

    public se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration findByName(String providerName) {
        ProviderConfiguration providerConfiguration = providerConfigurationByName.get(providerName);
        return StorageProviderConfigurationConverter.convert(providerConfiguration, getProviderStatus(providerConfiguration));
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
        ProviderStatusConfiguration psc = providerStatusConfigurationRepository.findOne(providerConfiguration.getName());
        return Optional.ofNullable(psc);
    }

    @Override
    public void updateStatus(String providerName, ProviderStatuses providerStatus) {
        ProviderStatusConfiguration providerStatusConfiguration = providerStatusConfigurationRepository.getOne(providerName);
        if (Objects.isNull(providerStatusConfiguration)){
            log.warn("Provider {} could not be found.", providerName);
            return;
        }

        ProviderStatuses oldStatus = providerStatusConfiguration.getStatus();
        if (oldStatus.equals(providerStatus)) {
            log.warn("Provider {} already has status {}", providerName, providerStatus);
            return;
        }

        providerStatusConfiguration.setStatus(providerStatus);
        providerStatusConfigurationRepository.save(providerStatusConfiguration);
        log.info("Provider status updated - Provider name: {}, old status: {}, new status: {}",
                providerName, providerStatusConfiguration.getStatus(), oldStatus, providerStatus);
    }
}
