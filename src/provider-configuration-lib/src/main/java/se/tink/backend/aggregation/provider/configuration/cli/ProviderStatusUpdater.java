package se.tink.backend.aggregation.provider.configuration.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationDAO;
import se.tink.backend.core.ProviderStatuses;

// FIXME: move business logic to controller instead of using DAO directly
public class ProviderStatusUpdater {
    private static final Logger log = LoggerFactory.getLogger(ProviderStatusUpdater.class);

    private final ProviderConfigurationDAO providerConfigurationDAO;

    public ProviderStatusUpdater(ProviderConfigurationDAO providerConfigurationDAO) {
        this.providerConfigurationDAO = providerConfigurationDAO;
    }

    public void update(String providerName, ProviderStatuses providerStatus) {
        log.info(String.format("Attempting to update status of provider '%s' to '%s'",
                providerName, providerStatus));
        providerConfigurationDAO.updateStatus(providerName, providerStatus);
    }
}
