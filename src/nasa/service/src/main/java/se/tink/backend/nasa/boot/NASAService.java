package se.tink.backend.nasa.boot;

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.tink.backend.libraries.healthcheckhandler.HealthCheckHandler;
import se.tink.backend.libraries.httpserver.SimpleHTTPServer;
import se.tink.backend.nasa.boot.configuration.Configuration;
import se.tink.backend.nasa.boot.configuration.ConfigurationUtils;
import se.tink.backend.nasa.boot.configuration.NASAServiceModule;
import se.tink.backend.nasa.boot.configuration.SensitiveConfiguration;
import se.tink.backend.nasa.metrics.PrometheusExportServer;
import spark.Filter;
import spark.Spark;

class NASAService {
    private static final Logger logger = LogManager.getLogger(NASAService.class);

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            logger.error(
                    "Unexpected argument count. Expected exactly one arguments. "
                            + "The first argument should be the path to the configuration file.");
        }

        try {
            new NASAService(
                    ConfigurationUtils.getConfiguration(args[0], Configuration.class),
                    new SensitiveConfiguration());
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
            System.exit(1);
        }
    }

    private final CountDownLatch keepRunningLatch;
    private SimpleHTTPServer httpServer;
    private Injector injector;
    private NasaRequestHandler nasaRequestHandler;

    NASAService(Configuration config, SensitiveConfiguration sensitiveConfiguration)
            throws Exception {
        injector =
                Guice.createInjector(
                        ImmutableList.of(new NASAServiceModule(config, sensitiveConfiguration)));
        logger.debug("Starting Integration Service");
        logger.debug("Built with Java " + System.getProperty("java.version"));

        keepRunningLatch = new CountDownLatch(1);
        start(config, sensitiveConfiguration);
        Runtime.getRuntime().addShutdownHook(new Thread(keepRunningLatch::countDown));
        keepRunningLatch.await(); // released from above thread on sigterm
        logger.info("Received signal to stop. Initiating shutdown");
        stop();
    }

    private void start(Configuration config, SensitiveConfiguration sensitiveConfiguration)
            throws Exception {
        logger.info("Starting Servers");

        nasaRequestHandler = NasaRequestHandler.CreateNasaRequestHandler();

        // Start HTTP health check service
        httpServer = new SimpleHTTPServer(8080);
        httpServer.addContext(
                "/alive",
                new HealthCheckHandler(() -> {})); // just an connection alive check ("ping")
        httpServer.addContext(
                "/healthy",
                new HealthCheckHandler(
                        () -> {
                            /* check DB */
                            logger.debug("Database is Healthy!");
                        },
                        () -> {
                            /* check something else */
                        }));
        httpServer.start();

        // Start the Prometheus Exporter Server
        injector.getInstance(PrometheusExportServer.class).start();

        // Setup spark HTTPS server
        Spark.port(config.getPort());

        Spark.secure(
                config.getKeyStorePath(),
                sensitiveConfiguration.getKeyStorePassword(),
                config.getCertAlias(),
                config.getTrustStorePath(),
                sensitiveConfiguration.getTrustStorePassword(),
                config.isValidateCerts());

        Spark.before(SparkFilters.ACCESS_LOGGING);

        Spark.get("/ping", (request, response) -> nasaRequestHandler.ping(request, response));
        Spark.get(
                "/initiate", (request, response) -> nasaRequestHandler.initiate(request, response));

        // Log the path and the body of requests to the Aggregation Controller API
        Spark.before("/aggregation/controller/v1/system/*", SparkFilters.REQUEST_LOGGING);

        AggregationControllerApiFactory.createUpdateServiceEndpoints();
        AggregationControllerApiFactory.createProcessServiceEndpoints();
        AggregationControllerApiFactory.createCredentialsServiceEndpoints();
    }

    private void stop() throws Exception {
        int duration = 10;
        TimeUnit unit = TimeUnit.SECONDS;

        Stopwatch sw = Stopwatch.createStarted();

        final CountDownLatch shutdownLatch =
                new CountDownLatch(3); // same numbers as components to close

        // Stop the Prometheus Exporter Server
        injector.getInstance(PrometheusExportServer.class).stop();

        // Stop Spark HTTPS server
        Spark.stop();

        // Stop simple http server for health checks
        httpServer.stop(shutdownLatch, duration, unit);

        shutdownLatch.await(duration + 1, unit);

        logger.info("Shutdown took: " + sw.stop().elapsed(TimeUnit.MILLISECONDS) + "ms");
    }

    private static class SparkFilters {
        static Filter ACCESS_LOGGING =
                (request, response) ->
                        logger.info(
                                "Request info: {} {} {} {}",
                                request.ip(),
                                request.userAgent(),
                                request.requestMethod(),
                                request.url());

        static Filter REQUEST_LOGGING =
                (request, response) ->
                        logger.info(
                                "Request to path: {} - body: {}",
                                request.pathInfo(),
                                request.body());
    }
}
