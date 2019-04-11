package se.tink.backend.aggregation.cli;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationCommonModule;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationConfigurationModule;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationRepositoryModule;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.libraries.discovery.CoordinationModule;

public abstract class AggregationServiceContextCommand<T extends AggregationServiceConfiguration>
        extends ConfiguredCommand<T> {
    private static final AggregationLogger log =
            new AggregationLogger(AggregationServiceContextCommand.class);

    protected AggregationServiceContextCommand(String name, String description) {
        super(name, description);
    }

    protected abstract void run(
            Bootstrap<T> bootstrap, Namespace namespace, T configuration, Injector injector)
            throws Exception;

    @Override
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration)
            throws Exception {
        List<AbstractModule> modules =
                Lists.newArrayList(
                        new AggregationCommonModule(),
                        new CoordinationModule(),
                        new AggregationConfigurationModule(configuration),
                        new AggregationRepositoryModule(configuration.getDatabase()));

        Injector injector = Guice.createInjector(modules);

        Exception commandExecutionError = null;
        Exception serviceShutdownError = null;
        try {
            run(bootstrap, namespace, configuration, injector);
        } catch (Exception e) {
            log.error("Something went wrong when executing command.", e);
            commandExecutionError = e;
        } finally {
            try {
                injector.getInstance(AggregationSpringContext.class).close();
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
