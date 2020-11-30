package se.tink.backend.aggregation.cli;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.controllers.ProvisionClientController;
import se.tink.backend.aggregation.storage.database.repositories.AggregatorConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClientConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.ClusterConfigurationsRepository;
import se.tink.backend.aggregation.storage.database.repositories.CryptoConfigurationsRepository;

public class AddClientConfigurationsCommand
        extends AggregationServiceContextCommand<AggregationServiceConfiguration> {

    public AddClientConfigurationsCommand() {
        super("add-client-configuration", "Adds a new client to the configuration databases.");
    }

    @Override
    protected void run(
            Bootstrap<AggregationServiceConfiguration> bootstrap,
            Namespace namespace,
            AggregationServiceConfiguration configuration,
            Injector injector)
            throws Exception {

        if (configuration.getProvisionClientsConfig() == null) {
            throw new Exception("Provision clients configuration should not be null.");
        }

        AggregatorConfigurationsRepository aggregatorConfigurationsRepository =
                injector.getInstance(AggregatorConfigurationsRepository.class);
        ClusterConfigurationsRepository clusterConfigurationsRepository =
                injector.getInstance(ClusterConfigurationsRepository.class);
        ClientConfigurationsRepository clientConfigurationsRepository =
                injector.getInstance(ClientConfigurationsRepository.class);
        CryptoConfigurationsRepository cryptoConfigurationsRepository =
                injector.getInstance(CryptoConfigurationsRepository.class);

        ProvisionClientController provisionClientController =
                new ProvisionClientController(
                        aggregatorConfigurationsRepository,
                        clusterConfigurationsRepository,
                        clientConfigurationsRepository,
                        cryptoConfigurationsRepository);

        provisionClientController.provision(configuration.getProvisionClientsConfig());
    }
}
