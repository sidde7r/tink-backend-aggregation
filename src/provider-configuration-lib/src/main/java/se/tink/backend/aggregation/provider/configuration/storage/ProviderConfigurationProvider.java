package se.tink.backend.aggregation.provider.configuration.storage;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.storage.converter.StorageProviderConfigurationConverter;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationDAO;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderStatusConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.repositories.ProviderStatusConfigurationRepository;
import se.tink.backend.core.ProviderStatuses;

import java.util.Collections;
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
            ProviderStatusConfigurationRepository providerStatusConfigurationRepository) {
        this.providerConfigurationByName = providerConfigurationByName;
        this.enabledProvidersOnCluster = enabledProvidersOnCluster;
        this.providerOverrideOnCluster = providerOverrideOnCluster;
        this.providerStatusConfigurationRepository = providerStatusConfigurationRepository;
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAll() {
        if (Objects.isNull(providerConfigurationByName)) {
            log.error("Provider Configuration by name map should not be null.");
            return Collections.emptyList();
        }

        if (providerConfigurationByName.values().isEmpty()) {
            log.error("Provider Configuration by name map should not be empty.");
        }

        Map<String, ProviderStatusConfiguration> allProviderStatuses = getAllProviderStatuses();

        return providerConfigurationByName.values().stream()
                .map(provider -> StorageProviderConfigurationConverter.convert(provider,
                        Optional.ofNullable(allProviderStatuses.get(provider.getName()))))
                .collect(Collectors.toList());
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByClusterId(
            String clusterId) {
        Set<String> providerNamesForCluster = enabledProvidersOnCluster.get(clusterId);
        if (Objects.isNull(providerNamesForCluster) || providerNamesForCluster.isEmpty()) {
            log.warn("Could not find any enabled providers for clusterId: " + clusterId);
            return Collections.emptyList();
        }

        Map<String, ProviderStatusConfiguration> allProviderStatuses = getAllProviderStatuses();

        return providerNamesForCluster.stream()
                .map(providerName -> getProviderConfigurationForCluster(clusterId, providerName))
                .map(provider -> StorageProviderConfigurationConverter.convert(provider,
                        Optional.ofNullable(allProviderStatuses.get(provider.getName()))))
                .collect(Collectors.toList());
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByMarket(
            String market) {

        Map<String, ProviderStatusConfiguration> allProviderStatuses = getAllProviderStatuses();

        return providerConfigurationByName.values().stream()
                .filter(providerConfiguration -> Objects.equals(market, providerConfiguration.getMarket()))
                .map(provider -> StorageProviderConfigurationConverter.convert(provider,
                        Optional.ofNullable(allProviderStatuses.get(provider.getName()))))
                .collect(Collectors.toList());
    }

    public se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration findByClusterIdAndProviderName(
            String clusterId, String providerName) {
        ProviderConfiguration providerConfiguration = getProviderConfigurationForCluster(clusterId, providerName);
        if (Objects.isNull(providerConfiguration)){
            log.warn("Could not find provider by name {} in cluster {} ", providerName, clusterId);
            return null;
        }

        return StorageProviderConfigurationConverter
                .convert(providerConfiguration, getProviderStatus(providerConfiguration));
    }

    public se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration findByName(
            String providerName) {
        ProviderConfiguration providerConfiguration = providerConfigurationByName.get(providerName);
        if (Objects.isNull(providerConfiguration)) {
            log.warn("Could not find provider by name: " + providerName);
            return null;
        }
        return StorageProviderConfigurationConverter
                .convert(providerConfiguration, getProviderStatus(providerConfiguration));
    }

    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByClusterIdAndMarket(
            String clusterId, String market) {
        return findAllByClusterId(clusterId).stream()
                .filter(providerConfiguration -> Objects.equals(market, providerConfiguration.getMarket()))
                .collect(Collectors.toList());
    }

    private ProviderConfiguration getProviderConfigurationForCluster(String clusterId, String providerName) {
        if (!enabledProvidersOnCluster.containsKey(clusterId) ||
                !enabledProvidersOnCluster.get(clusterId).contains(providerName)){
            return null;
        }
        if (providerOverrideOnCluster.containsKey(clusterId) &&
                providerOverrideOnCluster.get(clusterId).containsKey(providerName)) {
            return providerOverrideOnCluster.get(clusterId).get(providerName);
        } else {
            return providerConfigurationByName.get(providerName);
        }
    }

    private Map<String, ProviderStatusConfiguration> getAllProviderStatuses() {
        List<ProviderStatusConfiguration> providerStatusConfigurations = providerStatusConfigurationRepository.findAll();
        if (Objects.isNull(providerStatusConfigurations)) {
            return Collections.emptyMap();
        }

        return providerStatusConfigurations.stream()
                .collect(Collectors.toMap(
                        ProviderStatusConfiguration::getProviderName,
                        Function.identity()));
    }

    private Optional<ProviderStatusConfiguration> getProviderStatus(ProviderConfiguration providerConfiguration) {
        ProviderStatusConfiguration providerStatusConfiguration = providerStatusConfigurationRepository
                .findOne(providerConfiguration.getName());
        return Optional.ofNullable(providerStatusConfiguration);
    }

    @Override
    public void updateStatus(String providerName, ProviderStatuses providerStatus) {
        ProviderStatusConfiguration providerStatusConfiguration = providerStatusConfigurationRepository
                .getOne(providerName);
        if (Objects.isNull(providerStatusConfiguration)) {
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
