package se.tink.backend.main.commands;

import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.SpringContexts;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.guice.configuration.AllRepositoryModule;
import se.tink.backend.guice.configuration.ClientServiceFactoryModule;
import se.tink.backend.guice.configuration.CommandModule;
import se.tink.backend.guice.configuration.CommonModule;
import se.tink.backend.guice.configuration.ConfigurationModule;
import se.tink.backend.guice.configuration.EmailModule;
import se.tink.backend.guice.configuration.EventTrackerModule;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.discovery.CoordinationModule;

/**
 * Copied this class from System. This should be removed when we're no longer dependent on ServiceContext and each
 * command can inject what it needs.
 */
public abstract class ServiceContextCommand<T extends ServiceConfiguration> extends ConfiguredCommand<T> {

    private static final LogUtils log = new LogUtils(ServiceContextCommand.class);

    protected ServiceContextCommand(String name, String description) {
        super(name, description);
    }

    protected abstract void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration,
            Injector injector, ServiceContext serviceContext) throws Exception;

    @Override
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        List<AbstractModule> modules = Lists.newArrayList(
                new CommonModule(),
                new CoordinationModule(),
                new EventTrackerModule(),
                new ConfigurationModule(configuration),
                new CommandModule(),
                new AllRepositoryModule(configuration.getDatabase(), configuration.getDistributedDatabase()),
                new EmailModule(configuration.getEmail()),
                new ClientServiceFactoryModule());

        Injector injector = Guice.createInjector(modules);

        ServiceContext serviceContext = injector.getInstance(ServiceContext.class);

        try {
            serviceContext.start();
        } catch (Exception e) {
            throw new RuntimeException("Could not start service.", e);
        }

        Exception commandExecutionError = null;
        Exception serviceShutdownError = null;
        try {
            run(bootstrap, namespace, configuration, injector, serviceContext);
        } catch (Exception e) {
            log.error("Something went wrong when executing command.", e);
            commandExecutionError = e;
        } finally {
            try {
                serviceContext.stop();
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
