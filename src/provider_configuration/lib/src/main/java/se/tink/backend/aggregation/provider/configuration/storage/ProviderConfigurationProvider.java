package se.tink.backend.aggregation.provider.configuration.storage;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationCore;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationDAO;
import se.tink.backend.aggregation.provider.configuration.storage.converter.StorageProviderConfigurationConverter;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderConfigurationStorage;
import se.tink.backend.aggregation.provider.configuration.storage.models.ProviderStatusConfiguration;
import se.tink.backend.aggregation.provider.configuration.storage.repositories.ProviderStatusConfigurationRepository;
import se.tink.libraries.provider.enums.ProviderStatuses;

public class ProviderConfigurationProvider implements ProviderConfigurationDAO {
    private final Map<String, ProviderConfigurationStorage> providerConfigurationByName;
    private final ProviderStatusConfigurationRepository providerStatusConfigurationRepository;
    private final ClusterProviderHandler clusterProviderHandler;

    private static final Logger log = LoggerFactory.getLogger(ProviderConfigurationProvider.class);

    @Inject
    public ProviderConfigurationProvider(
            @Named("providerConfiguration")
                    Map<String, ProviderConfigurationStorage> providerConfigurationByName,
            ProviderStatusConfigurationRepository providerStatusConfigurationRepository,
            ClusterProviderHandler clusterProviderHandler) {
        this.providerConfigurationByName = providerConfigurationByName;
        this.providerStatusConfigurationRepository = providerStatusConfigurationRepository;
        this.clusterProviderHandler = clusterProviderHandler;
    }

    @Override
    public List<ProviderConfigurationCore> findAllByClusterId(String clusterId) {

        if (!clusterProviderHandler.validate(clusterId)) {
            log.error("Could not find any configuration for clusterId: " + clusterId);
            return Collections.emptyList();
        }

        Map<String, ProviderConfigurationStorage> providerConfigurations =
                clusterProviderHandler.getProviderConfigurationForCluster(clusterId);

        Map<String, ProviderStatuses> providerStatusMap = getProviderStatusMap();

        return StorageProviderConfigurationConverter.convert(
                providerConfigurations.values(), providerStatusMap);
    }

    @Override
    public ProviderConfigurationCore findByClusterIdAndProviderName(
            String clusterId, String providerName) {

        ProviderConfigurationStorage providerConfigurationStorage =
                clusterProviderHandler.getProviderConfiguration(clusterId, providerName);

        if (Objects.isNull(providerConfigurationStorage)) {
            log.warn("Could not find provider by name {} in cluster {} ", providerName, clusterId);
            return null;
        }

        return StorageProviderConfigurationConverter.convert(
                providerConfigurationStorage, getProviderStatus(providerConfigurationStorage));
    }

    // Although this is not the "optimal" way to do this operation, it doesn't seem like this
    // functionality is used today.
    // If we are going to use it more, we can make it perform better
    @Override
    public List<ProviderConfigurationCore> findAllByClusterIdAndMarket(
            String clusterId, String market) {
        return findAllByClusterId(clusterId).stream()
                .filter(
                        providerConfiguration ->
                                market.equalsIgnoreCase(providerConfiguration.getMarket()))
                .collect(Collectors.toList());
    }

    private Map<String, ProviderStatuses> getProviderStatusMap() {
        List<ProviderStatusConfiguration> providerStatusConfigurations =
                providerStatusConfigurationRepository.findAll();
        if (Objects.isNull(providerStatusConfigurations)) {
            return Collections.emptyMap();
        }

        return providerStatusConfigurations.stream()
                .collect(
                        Collectors.toMap(
                                ProviderStatusConfiguration::getProviderName,
                                ProviderStatusConfiguration::getStatus));
    }

    private Optional<ProviderStatuses> getProviderStatus(
            ProviderConfigurationStorage providerConfigurationStorage) {
        ProviderStatusConfiguration providerStatusConfiguration =
                providerStatusConfigurationRepository.findOne(
                        providerConfigurationStorage.getName());

        if (providerStatusConfiguration == null) {
            return Optional.empty();
        }

        return Optional.ofNullable(providerStatusConfiguration.getStatus());
    }

    private ProviderStatusConfiguration createProviderStatusConfiguration(
            String providerName, ProviderStatuses providerStatuses) {
        ProviderStatusConfiguration providerStatusConfiguration = new ProviderStatusConfiguration();
        providerStatusConfiguration.setProviderName(providerName);
        providerStatusConfiguration.setStatus(providerStatuses);

        return providerStatusConfiguration;
    }

    /**
     * update the global provider status
     *
     * @param providerName
     * @param providerStatus this method handles updating the provider status: disregard a non valid
     *     provider name disregard if user try to update status to ENABLED (we do not want to allow
     *     global enable TODO build DAOstatus that does not allow ENABLED add status if provider
     *     does not have global status replace status if provider has a different status give
     *     warning if user try to update to the same status
     */
    @Override
    public void updateStatus(String providerName, ProviderStatuses providerStatus) {
        if (!providerConfigurationByName.containsKey(providerName)) {
            log.warn("Provider name {} is not a valid provider.", providerName);
            return;
        }

        ProviderStatusConfiguration providerStatusConfiguration =
                providerStatusConfigurationRepository.findOne(providerName);

        if (Objects.equals(providerStatus, ProviderStatuses.ENABLED)) {
            log.error("Can not globally enable provider");
            return;
        }

        if (Objects.isNull(providerStatusConfiguration)) {
            providerStatusConfiguration =
                    createProviderStatusConfiguration(providerName, providerStatus);

            providerStatusConfigurationRepository.save(providerStatusConfiguration);
            log.info(
                    "Provider status updated - Provider name: old global status: NONE, new global status {}.",
                    providerName,
                    providerStatus);
            return;
        }

        ProviderStatuses oldStatus = providerStatusConfiguration.getStatus();
        if (oldStatus.equals(providerStatus)) {
            log.warn("Provider {} already has status {}", providerName, providerStatus);
            return;
        }

        providerStatusConfiguration.setStatus(providerStatus);
        providerStatusConfigurationRepository.save(providerStatusConfiguration);
        log.info(
                "Provider status updated - Provider name: {}, old global status: {}, new global status: {}",
                providerName,
                oldStatus,
                providerStatus);
    }

    @Override
    public void removeStatus(String providerName) {
        if (!providerConfigurationByName.containsKey(providerName)) {
            log.warn("Provider name {} is not a valid provider.", providerName);
            return;
        }

        ProviderStatusConfiguration providerStatusConfiguration =
                providerStatusConfigurationRepository.findOne(providerName);

        if (Objects.isNull(providerStatusConfiguration)) {
            log.error("Provider {} does not have a global status", providerName);
            return;
        }

        ProviderStatuses oldStatus =
                providerStatusConfigurationRepository.findOne(providerName).getStatus();
        providerStatusConfigurationRepository.delete(providerName);
        log.info("Provider {} has old global status: {}, now deleted", providerName, oldStatus);
    }
}
