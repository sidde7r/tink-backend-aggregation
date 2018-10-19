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
                .map(providerName -> {
                    ProviderConfiguration providerConfigurationForCluster = getProviderConfigurationForCluster(
                            clusterId, providerName);

                    if (Objects.isNull(providerConfigurationForCluster)) {
                        log.warn("Could not find configuration for provider name:[{}]. "
                                + "Either add the provider to the global configuration or remove the provider from "
                                + "available providers for cluster:[{}]", providerName, clusterId);
                    }

                    return providerConfigurationForCluster;
                })
                .filter(Objects::nonNull)
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

    private ProviderStatusConfiguration createProviderStatusConfiguration(String providerName,
                                                                          ProviderStatuses providerStatuses){
        ProviderStatusConfiguration providerStatusConfiguration = new ProviderStatusConfiguration();
        providerStatusConfiguration.setProviderName(providerName);
        providerStatusConfiguration.setStatus(providerStatuses);

        return providerStatusConfiguration;
    }

    /**
     * update the global provider status
     * @param providerName
     * @param providerStatus
     * this method handles updating the provider status:
     *   disregard a non valid provider name
     *   disregard if user try to update status to ENABLED (we do not want to allow global enable
     *   TODO build DAOstatus that does not allow ENABLED
     *   add status if provider does not have global status
     *   replace status if provider has a different status
     *   give warning if user try to update to the same status
     */
    @Override
    public void updateStatus(String providerName, ProviderStatuses providerStatus) {
        if (!providerConfigurationByName.containsKey(providerName)) {
            log.warn("Provider name {} is not a valid provider.", providerName);
            return;
        }

        ProviderStatusConfiguration providerStatusConfiguration = providerStatusConfigurationRepository
                .findOne(providerName);

        if (Objects.equals(providerStatus, ProviderStatuses.ENABLED)) {
                log.error("Can not globally enable provider");
                return;
        }

        if (Objects.isNull(providerStatusConfiguration)) {
            providerStatusConfiguration = createProviderStatusConfiguration(providerName, providerStatus);

            providerStatusConfigurationRepository.save(providerStatusConfiguration);
            log.info("Provider status updated - Provider name: old global status: NONE, new global status {}.", providerName, providerStatus);
            return;
        }

        ProviderStatuses oldStatus = providerStatusConfiguration.getStatus();
        if (oldStatus.equals(providerStatus)) {
            log.warn("Provider {} already has status {}", providerName, providerStatus);
            return;
        }

        providerStatusConfiguration.setStatus(providerStatus);
        providerStatusConfigurationRepository.save(providerStatusConfiguration);
        log.info("Provider status updated - Provider name: {}, old global status: {}, new global status: {}",
                providerName, oldStatus, providerStatus);
    }

    @Override
    public void removeStatus(String providerName) {
        if (!providerConfigurationByName.containsKey(providerName)) {
            log.warn("Provider name {} is not a valid provider.", providerName);
            return;
        }

        ProviderStatusConfiguration providerStatusConfiguration = providerStatusConfigurationRepository
                .findOne(providerName);

        if (Objects.isNull(providerStatusConfiguration)) {
            log.error("Provider {} does not have a global status", providerName);
            return;
        }

        ProviderStatuses oldStatus = providerStatusConfigurationRepository.findOne(providerName).getStatus();
        providerStatusConfigurationRepository.delete(providerName);
        log.info("Provider {} has old global status: {}, now deleted",
                providerName, oldStatus);
    }
}
