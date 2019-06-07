package se.tink.backend.aggregation.configuration;

import com.google.common.base.Preconditions;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;

/**
 * Validates configurations required for a sane service state.
 *
 * @see ConfigurationValidator#validate() throws exceptions if any of the validations fail.
 */
public final class ConfigurationValidator {
    private final Map<String, ClientConfiguration> clientConfigurations;
    private final Map<String, ClusterConfiguration> clusterConfigurations;
    private final Map<String, AggregatorConfiguration> aggregatorConfigurations;
    private final CryptoConfigurationDao cryptoConfigurationDao;

    @Inject
    ConfigurationValidator(
            @Named("clientConfigurationByClientKey")
                    Map<String, ClientConfiguration> clientConfigurations,
            @Named("clusterConfigurations") Map<String, ClusterConfiguration> clusterConfigurations,
            @Named("aggregatorConfiguration")
                    Map<String, AggregatorConfiguration> aggregatorConfigurations,
            CryptoConfigurationDao cryptoConfigurationDao) {
        this.clientConfigurations = clientConfigurations;
        this.clusterConfigurations = clusterConfigurations;
        this.aggregatorConfigurations = aggregatorConfigurations;
        this.cryptoConfigurationDao = cryptoConfigurationDao;

        validate();
    }

    private void validate() {
        clientConfigurations.forEach(this::validateClientConfiguration);
    }

    private void validateClientConfiguration(
            String clientApiKey, ClientConfiguration clientConfiguration) {
        validateClusterConfiguration(clientApiKey, clientConfiguration);
        validateAggregatorConfiguration(clientApiKey, clientConfiguration);
        validateCryptoConfiguration(clientApiKey, clientConfiguration);
    }

    private void validateCryptoConfiguration(
            String clientApiKey, ClientConfiguration clientConfiguration) {
        Preconditions.checkState(
                cryptoConfigurationDao
                        .getCryptoWrapperOfClientName(clientConfiguration.getClientName())
                        .getClientName()
                        .isPresent(),
                "Client Api Key [%s] is missing crypto configuration",
                clientApiKey);
    }

    private void validateAggregatorConfiguration(
            String clientApiKey, ClientConfiguration clientConfiguration) {
        Preconditions.checkNotNull(
                aggregatorConfigurations.get(clientConfiguration.getAggregatorId()),
                "Client Api Key [%s] is missing aggregator configuration",
                clientApiKey);
    }

    private void validateClusterConfiguration(
            String clientApiKey, ClientConfiguration clientConfiguration) {
        Preconditions.checkNotNull(
                clusterConfigurations.get(clientConfiguration.getClusterId()),
                "Client Api Key [%s] is missing cluster configuration",
                clientApiKey);
    }
}
