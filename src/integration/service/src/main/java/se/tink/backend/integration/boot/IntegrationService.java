package se.tink.backend.integration.boot;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import io.prometheus.client.hotspot.DefaultExports;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.tink.backend.integration.boot.configuration.Configuration;
import se.tink.backend.integration.boot.configuration.ConfigurationUtils;
import se.tink.backend.integration.boot.configuration.IntegrationServiceModule;
import se.tink.backend.integration.boot.configuration.SensitiveConfiguration;
import se.tink.backend.integration.gprcserver.configuration.GrpcServerModule;
import se.tink.backend.libraries.healthcheckhandler.HealthCheckHandler;
import se.tink.backend.libraries.httpserver.SimpleHTTPServer;
import se.tink.backend.integration.gprcserver.GrpcServer;
import se.tink.libraries.metrics.PrometheusExportServer;

class IntegrationService {
    private static final Logger logger = LogManager.getLogger(IntegrationService.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            logger.error("Unexpected argument count. Expected exactly one arguments. " +
                    "The first argument should be the path to the configuration file.");
        }

        try {
            new IntegrationService(
                    ConfigurationUtils.getConfiguration(args[0], Configuration.class),
                    new SensitiveConfiguration()
            );
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            System.exit(1);
        }
    }


    private final CountDownLatch keepRunningLatch;
    private SimpleHTTPServer httpServer;
    private GrpcServer grpcServer;
    private Injector injector;

    IntegrationService(Configuration config, SensitiveConfiguration sensitiveConfiguration) throws Exception {
        injector = Guice.createInjector(ImmutableList.of(new GrpcServerModule(), new IntegrationServiceModule(config, sensitiveConfiguration)));
        logger.debug("Starting Integration Service");
        logger.debug("Built with Java " + System.getProperty("java.version"));

        keepRunningLatch = new CountDownLatch(1);
        start(config, sensitiveConfiguration);
        Runtime.getRuntime().addShutdownHook(new Thread(keepRunningLatch::countDown));
        keepRunningLatch.await();  //released from above thread on sigterm
        logger.info("Received signal to stop. Initiating shutdown");
        stop();
    }

    private void start(Configuration config, SensitiveConfiguration sensitiveConfiguration) throws Exception {
        logger.info("Starting Servers");

        // Start HTTP health check service
        httpServer = new SimpleHTTPServer(8080);
        httpServer.addContext("/alive", new HealthCheckHandler(() -> {})); // just an connection alive check ("ping")
        httpServer.addContext("/healthy", new HealthCheckHandler(
                ()-> {/* check DB */ logger.debug("Database is Healthy!"); },
                ()-> {/* check something else */}
        ));
        httpServer.start();

        // Start the Prometheus Exporter Server
        DefaultExports.initialize();

        grpcServer = injector.getInstance(GrpcServer.class);
        injector.getInstance(PrometheusExportServer.class).start();

        // Serve over TLS if configured
        if (config.getGrpcTlsCertificatePath() != null) {
            grpcServer.useTransportSecurity(
                    new File(config.getGrpcTlsCertificatePath()),
                    new File(config.getGrpcTlsKeyPath())
            );
        }

        grpcServer.start();
    }

    private void stop() throws Exception {
        int duration = 10;
        TimeUnit unit = TimeUnit.SECONDS;

        Stopwatch sw = Stopwatch.createStarted();

        // stop prometheus
        injector.getInstance(PrometheusExportServer.class).stop();

        final CountDownLatch shutdownLatch = new CountDownLatch(2); // same numbers as components to close
        httpServer.stop(shutdownLatch, duration, unit);
        grpcServer.stop(shutdownLatch, duration, unit);

        shutdownLatch.await(duration + 1, unit);

        logger.info("Shutdown took: " + sw.stop().elapsed(TimeUnit.MILLISECONDS) + "ms");
    }
}

