package se.tink.backend.integration.boot;

import java.util.concurrent.CountDownLatch;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import io.grpc.BindableService;
import io.prometheus.client.hotspot.DefaultExports;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.tink.backend.integration.boot.configuration.Configuration;
import se.tink.backend.integration.boot.configuration.ConfigurationUtils;
import se.tink.backend.integration.boot.configuration.SensitiveConfiguration;
import se.tink.backend.libraries.healthcheckhandler.HealthCheckHandler;
import se.tink.backend.libraries.httpserver.SimpleHTTPServer;
import se.tink.backend.integration.gprcserver.GrpcServer;
import se.tink.backend.integration.pingservice.PingService;

class IntegrationService {
    private static final Logger logger = LogManager.getLogger(IntegrationService.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            logger.error("Unexpected argument count. Expected exactly two arguments. " +
                    "The first should be the configuration file, the second should be the sensitive configuration file.");
        }

        new IntegrationService(
                ConfigurationUtils.getConfiguration(args[0], Configuration.class),
                ConfigurationUtils.getConfiguration(args[1], SensitiveConfiguration.class)
        );
    }

    private final CountDownLatch keepRunningLatch;
    private SimpleHTTPServer httpServer;
    private GrpcServer grpcServer;
    private io.prometheus.client.exporter.HTTPServer prometheusServer;

    IntegrationService(Configuration config, SensitiveConfiguration sensitiveConfiguration) throws InterruptedException, IOException {
        logger.debug("Starting Integration Service");
        logger.debug("Built with Java " + System.getProperty("java.version"));

        keepRunningLatch = new CountDownLatch(1);
        start();
        Runtime.getRuntime().addShutdownHook(new Thread(keepRunningLatch::countDown));
        keepRunningLatch.await();  //released from above thread on sigterm
        logger.info("Received signal to stop. Initiating shutdown");
        stop();
    }

    private void start() throws IOException, InterruptedException {
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
        prometheusServer = new io.prometheus.client.exporter.HTTPServer(9130);

        // Start the gRPC Server
        List<? extends BindableService> services = ImmutableList.of(
                new PingService()
        );

        grpcServer = new GrpcServer(
                services,
                new InetSocketAddress(8889)
        );

        grpcServer.start();
    }

    private void stop() throws InterruptedException {
        int duration = 10;
        TimeUnit unit = TimeUnit.SECONDS;

        Stopwatch sw = Stopwatch.createStarted();

        prometheusServer.stop();
        final CountDownLatch shutdownLatch = new CountDownLatch(2); // same numbers as components to close
        httpServer.stop(shutdownLatch, duration, unit);
        grpcServer.stop(shutdownLatch, duration, unit);

        shutdownLatch.await(duration + 1, unit);

        logger.info("Shutdown took: " + sw.stop().elapsed(TimeUnit.MILLISECONDS) + "ms");
    }
}

