package se.tink.backend.aggregation.provider.configuration.controllers;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import javax.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.cluster.identifiers.ClusterId;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationCore;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationDAO;

public class ProviderServiceController {
    private static final Logger log = LoggerFactory.getLogger(ProviderServiceController.class);

    private final ProviderConfigurationDAO providerConfigurationDAO;

    @Inject
    public ProviderServiceController(ProviderConfigurationDAO providerConfigurationDAO) {
        this.providerConfigurationDAO = providerConfigurationDAO;
    }

    public List<ProviderConfigurationCore> list(ClusterId clusterId) {
        return providerConfigurationDAO.findAllByClusterId(clusterId.getId());
    }

    public List<ProviderConfigurationCore> listByMarket(ClusterId clusterId, String market) {
        return providerConfigurationDAO.findAllByClusterIdAndMarket(clusterId.getId(), market);
    }

    public Optional<ProviderConfigurationCore> getProviderByName(ClusterId clusterId, String providerName) {
        try {
            return Optional.of(providerConfigurationDAO.findByClusterIdAndProviderName(clusterId.getId(), providerName));
        } catch (NoResultException e) {
            log.warn("Could not find providerConfiguration for clusterId: {}, providerName: {}",
                    clusterId.getId(), providerName);
        }
        return Optional.empty();
    }
}
