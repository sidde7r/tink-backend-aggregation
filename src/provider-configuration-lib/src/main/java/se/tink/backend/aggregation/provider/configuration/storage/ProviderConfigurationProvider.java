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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class ProviderConfigurationProvider implements ProviderConfigurationDAO {
    private final Map<String, ProviderConfiguration> providerConfigurationByName;
    private final ProviderStatusConfigurationRepository providerStatusConfigurationRepository;
    private final ClusterProviderHandler clusterProviderHandler;

    private final static Logger log = LoggerFactory.getLogger(ProviderConfigurationProvider.class);

    @Inject
    public ProviderConfigurationProvider(
            @Named("providerConfiguration") Map<String, ProviderConfiguration> providerConfigurationByName,
            ProviderStatusConfigurationRepository providerStatusConfigurationRepository,
            ClusterProviderHandler clusterProviderHandler) {
        this.providerConfigurationByName = providerConfigurationByName;
        this.providerStatusConfigurationRepository = providerStatusConfigurationRepository;
        this.clusterProviderHandler = clusterProviderHandler;
    }

    @Override
    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByClusterId(
            String clusterId) {

        if (!clusterProviderHandler.validate(clusterId)) {
            log.error("Could not find any configuration for clusterId: " + clusterId);
            return Collections.emptyList();
        }

        Map<String, ProviderConfiguration> providerConfigurations = clusterProviderHandler.getProviderConfigurationForCluster(clusterId);

        List<ProviderStatusConfiguration> providerStatuses = getProviderStatuses();

        // Override the global status only for the values that we have in the database. (Which should be very few at a time.)
        providerStatuses.forEach(
                status  -> {
                    String providerName = status.getProviderName();
                    if (!providerConfigurations.containsKey(providerName)) {
                        return;
                    }
                    providerConfigurations.get(providerName).setStatus(status.getStatus());
                }
        );

        return StorageProviderConfigurationConverter.convert(providerConfigurations.values());
    }

    @Override
    public se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration findByClusterIdAndProviderName(
            String clusterId, String providerName) {

        ProviderConfiguration providerConfiguration = clusterProviderHandler.getProviderConfiguration(clusterId, providerName);

        if (Objects.isNull(providerConfiguration)){
            log.warn("Could not find provider by name {} in cluster {} ", providerName, clusterId);
            return null;
        }

        Optional<ProviderStatusConfiguration> providerStatusConfiguration = getProviderStatus(providerConfiguration);
        providerStatusConfiguration.ifPresent(providerStatusConfiguration1 -> providerConfiguration
                .setStatus(providerStatusConfiguration1.getStatus()));

        return StorageProviderConfigurationConverter.convert(providerConfiguration);
    }


    // Although this is not the "optimal" way to do this operation, it doesn't seem like this functionality is used today.
    // If we are going to use it more, we can make it perform better
    @Override
    public List<se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration> findAllByClusterIdAndMarket(
            String clusterId, String market) {
        return findAllByClusterId(clusterId).stream()
                .filter(providerConfiguration -> market.equalsIgnoreCase(providerConfiguration.getMarket()))
                .collect(Collectors.toList());
    }

    private List<ProviderStatusConfiguration> getProviderStatuses() {
        List<ProviderStatusConfiguration> providerStatusConfigurations = providerStatusConfigurationRepository.findAll();
        if (Objects.isNull(providerStatusConfigurations)) {
            return Collections.emptyList();
        }

        return providerStatusConfigurations;
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
