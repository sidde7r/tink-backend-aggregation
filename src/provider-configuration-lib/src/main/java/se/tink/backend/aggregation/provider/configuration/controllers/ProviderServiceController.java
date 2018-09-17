package se.tink.backend.aggregation.provider.configuration.controllers;

import com.google.inject.Inject;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import javax.persistence.NoResultException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.cluster.identification.ClusterId;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationDAO;

// FIXME: move localization outside controller
public class ProviderServiceController {
    private static final Logger log = LoggerFactory.getLogger(ProviderServiceController.class);

    private final ProviderConfigurationDAO providerConfigurationDAO;

    @Inject
    public ProviderServiceController(ProviderConfigurationDAO providerConfigurationDAO) {
        this.providerConfigurationDAO = providerConfigurationDAO;
    }

    public List<ProviderConfiguration> list(Locale locale) {
        List<ProviderConfiguration> providerConfigurationList = providerConfigurationDAO.findAll();
        return Localizator.translate(locale, providerConfigurationList);
    }

    public List<ProviderConfiguration> list(Locale locale, ClusterId clusterId) {
        List<ProviderConfiguration> providerConfigurationList = providerConfigurationDAO.findAllByClusterId(clusterId.getId());
        return Localizator.translate(locale, providerConfigurationList);
    }

    public List<ProviderConfiguration> listByMarket(Locale locale, ClusterId clusterId, String market) {
        List<ProviderConfiguration> providerConfigurationList = providerConfigurationDAO.findAllByClusterIdAndMarket(clusterId.getId(), market);
        return Localizator.translate(locale, providerConfigurationList);
    }

    public Optional<ProviderConfiguration> getProviderByName(Locale locale, ClusterId clusterId, String providerName) {
        try {
            return Optional.of(Localizator.translate(locale,
                    providerConfigurationDAO.findByClusterIdAndProviderName(clusterId.getId(), providerName)));
        } catch (NoResultException e) {
            log.warn("Could not find providerConfiguration for clusterId: %s, providerName: %s",
                    clusterId.getId(), providerName);
        }
        return Optional.empty();
    }
}
