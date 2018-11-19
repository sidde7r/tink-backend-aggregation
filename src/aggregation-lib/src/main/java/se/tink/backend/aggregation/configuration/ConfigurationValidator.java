package se.tink.backend.aggregation.configuration;

import com.google.common.base.Preconditions;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.storage.database.models.AggregatorConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClientConfiguration;
import se.tink.backend.aggregation.storage.database.models.ClusterConfiguration;

public final class ConfigurationValidator {
    @Inject
    ConfigurationValidator(
            @Named("clientConfigurationByClientKey") Map<String, ClientConfiguration> clientConfigurations,
            @Named("clusterConfigurations") Map<String, ClusterConfiguration> clusterConfigurations,
            @Named("aggregatorConfiguration") Map<String, AggregatorConfiguration> aggregatorConfigurations,
            CryptoConfigurationDao cryptoConfigurationDao) {

        clientConfigurations.forEach((clientApiKey, clientConfiguration) -> {
            Preconditions.checkNotNull(
                    clusterConfigurations.get(
                            clientConfiguration.getClusterId()),
                    "Client Api Key [%s] is missing cluster configuration", clientApiKey);

            Preconditions.checkNotNull(
                    aggregatorConfigurations.get(
                            clientConfiguration.getAggregatorId()),
                    "Client Api Key [%s] is missing aggregator configuration", clientApiKey);

            Preconditions.checkState(
                    cryptoConfigurationDao.getCryptoWrapperOfClientName(
                            clientConfiguration.getClientName()).getClientName().isPresent(),
                    "Client Api Key [%s] is missing crypto configuration", clientApiKey);
        });
    }
}
