package se.tink.backend.aggregation.configuration;

import com.google.inject.Inject;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.models.AggregationDevelopmentConfiguration;
import se.tink.backend.aggregation.storage.database.models.CryptoConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;

public class DevelopmentConfigurationSeeder {
    private static final Logger log = LoggerFactory.getLogger(DevelopmentConfigurationSeeder.class);
    private final CryptoConfigurationsRepository cryptoConfigurationsRepository;
    private final AggregationDevelopmentConfiguration developmentConfiguration;

    @Inject
    DevelopmentConfigurationSeeder(
            CryptoConfigurationsRepository cryptoConfigurationsRepository,
            AggregationDevelopmentConfiguration developmentConfiguration) {
        this.cryptoConfigurationsRepository = cryptoConfigurationsRepository;
        this.developmentConfiguration = developmentConfiguration;
        seedCryptoConfiguration();
    }

    private void seedCryptoConfiguration() {
        CryptoConfiguration cryptoConfiguration = developmentConfiguration.getCryptoConfiguration();

        // TODO maybe we should add isValid methods in the new methods to check if data is in valid
        // format
        if (Objects.isNull(cryptoConfiguration)) {
            return;
        }

        CryptoConfiguration cryptoConfigurationInStorage =
                cryptoConfigurationsRepository.findOne(
                        cryptoConfiguration.getCryptoConfigurationId());

        if (!Objects.isNull(cryptoConfigurationInStorage)) {
            return;
        }

        log.info("Seeding crypto configuration for local development.");
        cryptoConfigurationsRepository.save(cryptoConfiguration);
    }
}
