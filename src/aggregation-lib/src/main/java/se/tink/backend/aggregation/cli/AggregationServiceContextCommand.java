package se.tink.backend.aggregation.cli;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.aggregation.guice.configuration.AggregationMultiClientRepositoryModule;
import se.tink.backend.aggregation.guice.configuration.AggregationSingleClientRepositoryModule;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.guice.configuration.CommonModule;
import se.tink.backend.guice.configuration.ConfigurationModule;
import se.tink.libraries.discovery.CoordinationModule;

public abstract class AggregationServiceContextCommand<T extends ServiceConfiguration> extends ConfiguredCommand<T> {
    private static final AggregationLogger log = new AggregationLogger(AggregationServiceContextCommand.class);

    protected AggregationServiceContextCommand(String name, String description) {
        super(name, description);
    }

    protected abstract void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration,
            Injector injector) throws Exception;

    @Override
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        List<AbstractModule> modules = Lists.newArrayList(
                new CommonModule(),
                new CoordinationModule(),
                new ConfigurationModule(configuration),
                new AggregationMultiClientRepositoryModule(configuration.getDatabase()));

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
        if (commandExecutionError != null)
        {
            throw commandExecutionError;
        } else if (serviceShutdownError != null) {
            throw serviceShutdownError;
        }
    }
}
