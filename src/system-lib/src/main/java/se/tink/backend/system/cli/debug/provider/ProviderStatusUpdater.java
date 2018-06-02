package se.tink.backend.system.cli.debug.provider;

import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.utils.LogUtils;

public class ProviderStatusUpdater {
    private static final LogUtils LOGGER = new LogUtils(ProviderStatusUpdater.class);

    private final ProviderRepository providerRepository;

    public ProviderStatusUpdater(ProviderRepository providerRepository) {
        this.providerRepository = providerRepository;
    }

    public void update(String providerName, ProviderStatuses providerStatus) {
        LOGGER.info(String.format("Attempting to update status of provider '%s' to '%s'",
                providerName, providerStatus));

        Provider provider = providerRepository.findByName(providerName);
        if (provider != null) {
            provider.setStatus(providerStatus);
            providerRepository.saveAndFlush(provider);
            LOGGER.info("Provider status updated");
        } else {
            LOGGER.warn(String.format("Provider '%s' could not be found.", providerName));
        }
    }
}
