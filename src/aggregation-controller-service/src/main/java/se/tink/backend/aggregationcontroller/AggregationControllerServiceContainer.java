package se.tink.backend.aggregationcontroller;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.apache.curator.framework.CuratorFramework;
import se.tink.backend.aggregationcontroller.configuration.AggregationControllerConfiguration;
import se.tink.backend.aggregationcontroller.configuration.AggregationControllerModuleFactory;
import se.tink.libraries.auth.ApiTokenAuthorizationHeaderPredicate;
import se.tink.libraries.auth.ContainerAuthorizationResourceFilterFactory;
import se.tink.libraries.discovery.ServiceDiscoveryHelper;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;

public class AggregationControllerServiceContainer extends Application<AggregationControllerConfiguration> {

    public static final String SERVICE_NAME = "aggregation-controller";

    public static void main(String[] args) throws Exception {
        new AggregationControllerServiceContainer().run(args);
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }

    @Override
    public void initialize(Bootstrap<AggregationControllerConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void run(AggregationControllerConfiguration configuration, Environment environment) throws Exception {
        // Add a dummy health check to avoid an annoying warning on startup.
        environment.healthChecks().register("cache", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

        if (configuration.getServiceAuthentication() != null
                && !configuration.getServiceAuthentication().getServerTokens().isEmpty()) {
            Predicate<String> authorizationAuthorizers = Predicates.or(
                    new ApiTokenAuthorizationHeaderPredicate(configuration.getServiceAuthentication()
                            .getServerTokens()));
            environment.jersey().getResourceConfig().getResourceFilterFactories()
                    .add(new ContainerAuthorizationResourceFilterFactory(authorizationAuthorizers));
        }

        Iterable<Module> modules = AggregationControllerModuleFactory.build(configuration, environment.jersey());
        Injector injector = DropwizardLifecycleInjectorFactory.build(environment.lifecycle(), modules);

        // Register the service to ZooKeeper for service discovery
        environment.lifecycle().manage(
                constructServiceDiscoveryHelperFromConfiguration(
                        injector.getInstance(CuratorFramework.class), configuration, SERVICE_NAME));
    }

    private ServiceDiscoveryHelper constructServiceDiscoveryHelperFromConfiguration(
            CuratorFramework coordinationClient, AggregationControllerConfiguration configuration, String serviceName) {
        final List<ConnectorFactory> applicationConnectors = ((DefaultServerFactory) configuration.getServerFactory())
                .getApplicationConnectors();

        return new ServiceDiscoveryHelper(
                coordinationClient,
                configuration.getCoordination(),
                serviceName,
                tryFindFirstNonTLSPort(applicationConnectors),
                tryFindFirstTLSPort(applicationConnectors));
    }

    private Optional<Integer> tryFindFirstNonTLSPort(List<ConnectorFactory> list) {
        return list.stream()
                .filter(l -> Predicates.instanceOf(HttpConnectorFactory.class).apply(l))
                .filter(l -> !Predicates.instanceOf(HttpsConnectorFactory.class).apply(l))
                .map(l -> se.tink.backend.utils.guavaimpl.Functions.cast(HttpConnectorFactory.class).apply(l))
                .filter(Objects::nonNull)
                .map(HttpConnectorFactory::getPort)
                .findFirst();
    }

    private Optional<Integer> tryFindFirstTLSPort(List<ConnectorFactory> list) {
        return list.stream()
                .filter(l -> Predicates.instanceOf(HttpsConnectorFactory.class).apply(l))
                .map(l -> se.tink.backend.utils.guavaimpl.Functions.cast(HttpsConnectorFactory.class).apply(l))
                .filter(Objects::nonNull)
                .map(HttpConnectorFactory::getPort)
                .findFirst();
    }
}
