package se.tink.backend.aggregation.cli;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.cli.EnvironmentCommand;
import io.dropwizard.setup.Environment;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.jetty.JettyStatisticsCollector;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;

/**
 * Runs a application as an HTTP server.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 */
public class TinkServerCommand<T extends AggregationServiceConfiguration>
        extends EnvironmentCommand<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TinkServerCommand.class);

    private final Class<T> configurationClass;

    public TinkServerCommand(Application<T> application) {
        super(application, "tink_server", "Runs the Dropwizard application as an HTTP server");
        this.configurationClass = application.getConfigurationClass();
    }

    /*
     * Since we don't subclass ServerCommand, we need a concrete reference to the configuration
     * class.
     */
    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    protected void run(Environment environment, Namespace namespace, T configuration)
            throws Exception {

        LOGGER.info(
                "Running TinkServerCommand instead of default ServerCommand. This is a copy of the default one, but attaches Jetty Metrics");

        final Server server = configuration.getServerFactory().build(environment);

        Handler topLevelHandler = server.getHandler();
        CollectorRegistry collectorRegistry = configuration.getCollectorRegistry();
        if (topLevelHandler instanceof StatisticsHandler && collectorRegistry != null) {

            LOGGER.info("Attaching JettyStatisticsCollector!");

            JettyStatisticsCollector jettyStatisticsCollector =
                    new JettyStatisticsCollector((StatisticsHandler) topLevelHandler);
            jettyStatisticsCollector.register(collectorRegistry);
        }

        try {
            server.addLifeCycleListener(new LifeCycleListener());
            cleanupAsynchronously();
            server.start();
        } catch (Exception e) {
            LOGGER.error("Unable to start server, shutting down", e);
            server.stop();
            cleanup();
            throw e;
        }
    }

    private class LifeCycleListener extends AbstractLifeCycle.AbstractLifeCycleListener {
        @Override
        public void lifeCycleStopped(LifeCycle event) {
            cleanup();
        }
    }
}
