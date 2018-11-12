package se.tink.backend.aggregation.configuration;

import com.google.inject.Inject;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.models.AggregationDevelopmentConfiguration;
import se.tink.backend.aggregation.storage.database.repositories.ClusterCryptoConfigurationRepository;
import se.tink.backend.core.ClusterCryptoConfiguration;

public class DevelopmentConfigurationSeeder {
    private static final Logger log = LoggerFactory.getLogger(DevelopmentConfigurationSeeder.class);

    @Inject
    DevelopmentConfigurationSeeder(ClusterCryptoConfigurationRepository clusterCryptoConfigurationRepository,
            AggregationDevelopmentConfiguration developmentConfiguration) {

        ClusterCryptoConfiguration cryptoConfiguration = developmentConfiguration.getClusterCryptoConfiguration();

        if (Objects.isNull(cryptoConfiguration) || !cryptoConfiguration.isValid()) {
            return;
        }

        ClusterCryptoConfiguration cryptoConfigurationInStorage = clusterCryptoConfigurationRepository.findOne(
                cryptoConfiguration.getCryptoId());

        if (!Objects.isNull(cryptoConfigurationInStorage)) {
            return;
        }

        log.info("Seeding cluster crypto configuration for local development.");
        clusterCryptoConfigurationRepository.save(cryptoConfiguration);
    }
}
