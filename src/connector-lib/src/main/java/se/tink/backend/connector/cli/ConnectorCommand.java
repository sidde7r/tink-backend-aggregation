package se.tink.backend.connector.cli;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Stage;
import com.netflix.governator.guice.LifecycleInjector;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.SpringContexts;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.connector.configuration.CommonConnectorModule;
import se.tink.backend.connector.configuration.ConnectorModule;
import se.tink.backend.connector.configuration.ConnectorRepositoryModule;
import se.tink.backend.connector.configuration.ConnectorServiceModule;
import se.tink.libraries.cluster.Cluster;
import se.tink.backend.guice.configuration.ConfigurationModule;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.discovery.CoordinationModule;

public abstract class ConnectorCommand<T extends ServiceConfiguration> extends ConfiguredCommand<T> {

    private static final LogUtils log = new LogUtils(ConnectorCommand.class);

    ConnectorCommand(String name, String description) {
        super(name, description);
    }

    protected abstract void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration,
            Injector injector) throws Exception;

    @Override
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        List<AbstractModule> modules = Lists.newArrayList(
                new CoordinationModule(),
                new ConfigurationModule(configuration),
                new ConnectorRepositoryModule(configuration.getDatabase(), configuration.getDistributedDatabase()));

        if (configuration.getCluster() != Cluster.ABNAMRO && configuration.getCluster() != Cluster.SEB) {
            modules.addAll(Lists.newArrayList(
                    new CommonConnectorModule(configuration),
                    new ConnectorServiceModule(),
                    new ConnectorModule()));
        }

        Injector injector = LifecycleInjector.builder()
                .inStage(Stage.PRODUCTION)
                .withModules(modules)
                .build().createInjector();

        Exception commandExecutionError = null;
        Exception serviceShutdownError = null;
        try {
            run(bootstrap, namespace, configuration, injector);
        } catch (Exception e) {
            log.error("Something went wrong when executing command.", e);
            commandExecutionError = e;
        } finally {
            try {
                injector.getInstance(SpringContexts.class).close();
            } catch (Exception e) {
                // Not throwing exception here to no
                log.error("Could not stop service gracefully.", e);
                serviceShutdownError = e;
            }
        }

        // Prefer to throw this since it's usually more informative.
        if (commandExecutionError != null) {
            throw commandExecutionError;
        } else if (serviceShutdownError != null) {
            throw serviceShutdownError;
        }
    }
}
