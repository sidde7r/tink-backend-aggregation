package se.tink.backend.common;

import com.google.common.base.Predicates;
import com.sun.jersey.api.core.ResourceConfig;
import io.dropwizard.Application;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.setup.Environment;
import io.protostuff.runtime.DefaultIdStrategy;
import io.protostuff.runtime.RuntimeEnv;
import io.swagger.jaxrs.listing.ApiListingResource;
import io.swagger.jaxrs.listing.SwaggerSerializers;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.health.DummyHealthCheck;
import se.tink.libraries.jersey.logging.AccessLoggingFilter;
import se.tink.libraries.jersey.logging.ResourceTimerFilterFactory;
import se.tink.libraries.http.client.RequestTracingFilter;
import se.tink.backend.serialization.protobuf.BigDecimalDelegate;
import se.tink.backend.serialization.protobuf.DateDelegate;
import se.tink.backend.serialization.protobuf.ProtobufMessageBodyReader;
import se.tink.backend.serialization.protobuf.ProtobufMessageBodyWriter;
import se.tink.backend.serialization.protobuf.URIDelegate;
import se.tink.backend.serialization.protobuf.UUIDDelegate;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.discovery.ServiceDiscoveryHelper;
import se.tink.libraries.dropwizard.ObjectMapperFactory;

/**
 * Tink (domain) specific service container
 * <p>
 * Responsible for
 * <ul>
 * <li>Initializing a {@link ServiceContext} used throughout the lifetime of the service container.
 * <li>Adding global filters we would like to run for all HTTP requests.
 * </ul>
 */
public abstract class AbstractServiceContainer extends Application<ServiceConfiguration> {

    private static final LogUtils log = new LogUtils(AbstractServiceContainer.class);

    static {
        // Sending enums by name instead of ints when using protobuf
        // We need to set enum int values on frontend before we can omit this flag
        System.setProperty("protostuff.runtime.enums_by_name", "true");
    }

    @SuppressWarnings("unchecked")
    @Override
    public final void run(ServiceConfiguration configuration, Environment environment) throws Exception {
        final JerseyEnvironment jersey = environment.jersey();
        ResourceConfig resourceConfig = jersey.getResourceConfig();
        RequestTracingFilter requestTracingFilter = new RequestTracingFilter();
        resourceConfig.getContainerRequestFilters().add(requestTracingFilter);

        log.info("Default encoding: " + Charset.defaultCharset());

        // Add the custom message body providers for Protocol Buffers support.

        jersey.register(ProtobufMessageBodyWriter.class);
        jersey.register(ProtobufMessageBodyReader.class);

        if (!(RuntimeEnv.ID_STRATEGY instanceof DefaultIdStrategy)) {
            throw new IllegalStateException("ProtoStuff Id strategy has been changed from Default.");
        }

        ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY).registerDelegate(new DateDelegate());
        ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY).registerDelegate(new UUIDDelegate());
        ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY).registerDelegate(new URIDelegate());
        ((DefaultIdStrategy) RuntimeEnv.ID_STRATEGY).registerDelegate(new BigDecimalDelegate());

        ObjectMapperFactory.configureForApiUse(environment.getObjectMapper());

        // Add a dummy healthcheck to avoid an annoying warning on startup.
        environment.healthChecks().register("cache", new DummyHealthCheck());

        jersey.register(new ApiListingResource());
        jersey.register(new SwaggerSerializers());

        // If an exception is thrown inside Service.run() dropwizard will not shutdown and will end up stuck before the
        // application has started.
        //
        // If we get an exception during boot, the best course of action is to stop the application completely and try
        // to start it again later.
        //
        // The 10s sleep is there to give time for all logs to be flushed.
        try {
            build(configuration, environment);
        } catch (Exception e) {
            log.error("Exception during Dropwizard boot", e);
            Thread.sleep(Duration.ofSeconds(10).toMillis());
            System.exit(1);
        }

        resourceConfig.getContainerResponseFilters().add(requestTracingFilter);
    }

    protected abstract void build(ServiceConfiguration configuration, Environment environment) throws Exception;

    private static Optional<Integer> tryFindFirstNonTLSPort(List<ConnectorFactory> list) {
        return list.stream()
                .filter(l -> Predicates.instanceOf(HttpConnectorFactory.class).apply(l))
                .filter(l -> !Predicates.instanceOf(HttpsConnectorFactory.class).apply(l))
                .map(l -> se.tink.backend.utils.guavaimpl.Functions.cast(HttpConnectorFactory.class).apply(l))
                .map(HttpConnectorFactory::getPort)
                .findFirst();
    }

    private static Optional<Integer> tryFindFirstTLSPort(List<ConnectorFactory> list) {
        return list.stream()
                .filter(l -> Predicates.instanceOf(HttpsConnectorFactory.class).apply(l))
                .map(l -> se.tink.backend.utils.guavaimpl.Functions.cast(HttpsConnectorFactory.class).apply(l))
                .map(HttpConnectorFactory::getPort)
                .findFirst();
    }

    protected static ServiceDiscoveryHelper constructServiceDiscoveryHelperFromConfiguration(
            CuratorFramework coordinationClient,
            ServiceConfiguration configuration, String serviceName) {
        final List<ConnectorFactory> applicationConnectors = ((DefaultServerFactory) configuration.getServerFactory())
                .getApplicationConnectors();

        return new ServiceDiscoveryHelper(
                coordinationClient,
                configuration.getCoordination(),
                serviceName,
                tryFindFirstNonTLSPort(applicationConnectors),
                tryFindFirstTLSPort(applicationConnectors)); // Temporarily disabled to debug TLS.
    }

    protected static void addRequestFilters(Environment environment, ResourceTimerFilterFactory resourceTimerFilter) {
        AccessLoggingFilter accessLoggingFilter = new AccessLoggingFilter();
        environment.jersey().getResourceConfig().getContainerRequestFilters().add(accessLoggingFilter);
        environment.jersey().getResourceConfig().getResourceFilterFactories().add(resourceTimerFilter);
        environment.jersey().getResourceConfig().getContainerResponseFilters().add(accessLoggingFilter);
    }
}
