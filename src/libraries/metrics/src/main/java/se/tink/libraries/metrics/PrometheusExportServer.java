package se.tink.libraries.metrics;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import com.google.inject.Inject;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.MetricsServlet;
import io.prometheus.client.hotspot.ClassLoadingExports;
import io.prometheus.client.hotspot.GarbageCollectorExports;
import io.prometheus.client.hotspot.MemoryPoolsExports;
import io.prometheus.client.hotspot.StandardExports;
import io.prometheus.client.hotspot.ThreadExports;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.LoggerFactory;

public class PrometheusExportServer {
    private static final org.slf4j.Logger log =
            LoggerFactory.getLogger(PrometheusExportServer.class);
    private final Server server;
    private final CollectorRegistry registry = new CollectorRegistry(true);
    private final MetricCollector collector;

    @Inject
    private PrometheusExportServer(PrometheusConfiguration config, MetricCollector collector) {
        this.collector = collector;
        this.server = new Server(config.getPort());
    }

    public static void start(PrometheusConfiguration config, MetricCollector collector)
            throws Exception {
        PrometheusExportServer server = new PrometheusExportServer(config, collector);
        server.start();
    }

    @PostConstruct
    public void start() throws Exception {
        registerLogback();
        registerJvmMetrics();
        registry.register(collector);

        ServletContextHandler context = new ServletContextHandler();
        context.setContextPath("/");
        context.addServlet(new ServletHolder(new MetricsServlet(registry)), "/metrics");

        server.setHandler(context);
        server.start();
        log.info("Started Prometheus exporter at " + server.getURI());
    }

    /**
     * Stop the HTTP server to free up the consumed port This enables the services run multiple
     * times without restarting the JVM
     */
    @PreDestroy
    public void stop() throws Exception {
        server.stop();
        log.debug("Stopped Prometheus exporter");
    }

    private void registerLogback() {
        final LoggerContext factory = (LoggerContext) LoggerFactory.getILoggerFactory();
        final Logger root = factory.getLogger(Logger.ROOT_LOGGER_NAME);

        final InstrumentedLogbackAppender metricsAppender =
                new InstrumentedLogbackAppender(registry);
        metricsAppender.setContext(root.getLoggerContext());
        metricsAppender.start();
        root.addAppender(metricsAppender);
    }

    private void registerJvmMetrics() {
        new StandardExports().register(registry);
        new MemoryPoolsExports().register(registry);
        new GarbageCollectorExports().register(registry);
        new ThreadExports().register(registry);
        new ClassLoadingExports().register(registry);
    }
}
