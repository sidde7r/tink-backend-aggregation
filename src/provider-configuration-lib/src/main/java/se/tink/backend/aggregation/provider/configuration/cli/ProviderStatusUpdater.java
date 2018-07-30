package se.tink.backend.aggregation.provider.configuration.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.repositories.mysql.ProviderConfigurationRepository;
import se.tink.backend.core.ProviderConfiguration;
import se.tink.backend.core.ProviderStatuses;

public class ProviderStatusUpdater {
    private static final Logger log = LoggerFactory.getLogger(ProviderStatusUpdater.class);

    private final ProviderConfigurationRepository providerRepository;

    public ProviderStatusUpdater(ProviderConfigurationRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public void update(String providerName, ProviderStatuses providerStatus) {
        log.info(String.format("Attempting to update status of provider '%s' to '%s'",
                providerName, providerStatus));

        ProviderConfiguration provider = providerRepository.findOne(providerName);
        if (provider != null) {
            provider.setStatus(providerStatus);
            providerRepository.saveAndFlush(provider);
            log.info("Provider status updated");
        } else {
            log.warn(String.format("Provider '%s' could not be found.", providerName));
        }
    }
}
