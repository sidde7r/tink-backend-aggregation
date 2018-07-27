package se.tink.backend.aggregation.cli.provider;

import se.tink.backend.common.repository.mysql.aggregation.providerconfiguration.ProviderConfigurationRepository;
import se.tink.backend.core.ProviderConfiguration;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.aggregation.log.AggregationLogger;

public class ProviderStatusUpdater {
    private static final AggregationLogger LOGGER = new AggregationLogger(ProviderStatusUpdater.class);

    private final ProviderConfigurationRepository providerRepository;

    public ProviderStatusUpdater(ProviderConfigurationRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public void update(String providerName, ProviderStatuses providerStatus) {
        LOGGER.info(String.format("Attempting to update status of provider '%s' to '%s'",
                providerName, providerStatus));

        ProviderConfiguration provider = providerRepository.findOne(providerName);
        if (provider != null) {
            provider.setStatus(providerStatus);
            providerRepository.saveAndFlush(provider);
            LOGGER.info("Provider status updated");
        } else {
            LOGGER.warn(String.format("Provider '%s' could not be found.", providerName));
        }
    }
}
