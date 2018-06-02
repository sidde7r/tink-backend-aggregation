package se.tink.backend.connector.configuration;

import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.connector.configuration.abn.AbnAmroConnectorModule;
import se.tink.backend.connector.configuration.seb.SebConnectorModule;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.guice.configuration.CommonModule;
import se.tink.backend.guice.configuration.ConfigurationModule;
import se.tink.libraries.discovery.CoordinationModule;

public class ConnectorModulesFactory {
    public static Iterable<Module> build(ServiceConfiguration configuration, JerseyEnvironment jersey) {
        return ImmutableList.<Module>builder()
                .add(
                        new CommonModule(),
                        new ConfigurationModule(configuration),
                        new CoordinationModule(),
                        new ConnectorRepositoryModule(configuration.getDatabase(),
                                configuration.getDistributedDatabase()),
                        new CommonConnectorModule(configuration))
                .addAll(clusterModules(configuration.getCluster(), jersey))
                .build();
    }

    private static ImmutableList<Module> clusterModules(Cluster cluster, JerseyEnvironment jersey) {
        switch (cluster) {
        case ABNAMRO:
            return ImmutableList.of(new AbnAmroConnectorModule(jersey));
        case CORNWALL:
            return ImmutableList.of(new SebConnectorModule(jersey));
        default:
            return ImmutableList
                    .of(new ConnectorServiceModule(), new ConnectorJerseyModule(jersey), new ConnectorModule());
        }
    }
}
