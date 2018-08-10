package se.tink.backend.aggregation;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import java.util.Objects;
import javax.ws.rs.Path;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.cli.provider.ChangeProviderRefreshFrequencyFactorCommand;
import se.tink.backend.aggregation.cli.provider.DebugProviderCommand;
import se.tink.backend.aggregation.cli.provider.ProviderStatusCommand;
import se.tink.backend.aggregation.cli.provider.SeedProvidersForMarketCommand;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.client.InProcessAggregationServiceFactory;
import se.tink.backend.aggregation.guice.configuration.AggregationModuleFactory;
import se.tink.backend.aggregation.resources.AggregationServiceResource;
import se.tink.backend.aggregation.resources.CreditSafeServiceResource;
import se.tink.backend.common.AbstractServiceContainer;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.queue.sqs.SqsConsumer;
import se.tink.libraries.auth.ApiTokenAuthorizationHeaderPredicate;
import se.tink.libraries.auth.ContainerAuthorizationResourceFilterFactory;
import se.tink.libraries.auth.YubicoAuthorizationHeaderPredicate;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.discovery.ServiceDiscoveryHelper;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;
import se.tink.libraries.metrics.MetricRegistry;

@Path("/aggregation")
public class AggregationServiceContainer extends AbstractServiceContainer {

    private static final ImmutableList<Command> COMMANDS = ImmutableList.of(
            new ChangeProviderRefreshFrequencyFactorCommand(),
            new DebugProviderCommand(),
            new ProviderStatusCommand(),
            new SeedProvidersForMarketCommand());

    public static void main(String[] args) throws Exception {
        new AggregationServiceContainer().run(args);
    }

    @Override
    public String getName() {
        return AggregationServiceFactory.SERVICE_NAME;
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
        COMMANDS.forEach(bootstrap::addCommand);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void build(ServiceConfiguration configuration, Environment environment) throws Exception {

        if (!configuration.isAggregationCluster() && configuration.getServiceAuthentication() != null
                && !configuration.getServiceAuthentication().getServerTokens().isEmpty()) {
            Predicate<String> authorizationAuthorizers = Predicates.or(
                    new ApiTokenAuthorizationHeaderPredicate(configuration.getServiceAuthentication()
                            .getServerTokens()),
                    new YubicoAuthorizationHeaderPredicate(
                            configuration.getYubicoClientId(),
                            configuration.getServiceAuthentication().getYubikeys()));
            environment.jersey().getResourceConfig().getResourceFilterFactories()
                    .add(new ContainerAuthorizationResourceFilterFactory(authorizationAuthorizers));
        }



        Injector injector = DropwizardLifecycleInjectorFactory.build(
                environment.lifecycle(),
                AggregationModuleFactory.build(configuration, environment.jersey()));
        environment.lifecycle().manage(injector.getInstance(SqsConsumer.class));
        ServiceContext serviceContext = injector.getInstance(ServiceContext.class);
        buildContainer(configuration, environment, serviceContext, injector);
    }

    private void buildContainer(ServiceConfiguration configuration, Environment environment,
            ServiceContext serviceContext, Injector injector) {

        if (!configuration.isAggregationCluster()) {
            // Check connectivity to encryption service. Will throw an exception if not reachable. Very important this check
            // is done before the aggregation container is fully functioning and its services are registered to Jersey etc.
            serviceContext.getEncryptionServiceFactory().getEncryptionService().ping();
        }

        final AggregationServiceResource aggregationServiceResource = new AggregationServiceResource(serviceContext,
                injector.getInstance(MetricRegistry.class), configuration.isUseAggregationController(),
                new AggregationControllerAggregationClient(
                        configuration.getEndpoints().getAggregationcontroller(),
                        serviceContext.getCoordinationClient()));
        environment.lifecycle().manage(aggregationServiceResource);

        CreditSafeServiceResource creditSafeServiceResource = new CreditSafeServiceResource(serviceContext);

        InProcessAggregationServiceFactory inProcessAggregationServiceFactory = (InProcessAggregationServiceFactory)
                serviceContext.getAggregationServiceFactory();

        inProcessAggregationServiceFactory.setAggregationService(aggregationServiceResource);
        inProcessAggregationServiceFactory.setCreditSafeService(creditSafeServiceResource);

        environment.jersey().register(inProcessAggregationServiceFactory.getAggregationService());

        if (Objects.equals(Cluster.TINK, configuration.getCluster()) && !configuration.isAggregationCluster()) {
            environment.jersey().register(inProcessAggregationServiceFactory.getCreditSafeService());
        }

        if (!configuration.isAggregationCluster()) {
            ServiceDiscoveryHelper serviceDiscoveryHelper = constructServiceDiscoveryHelperFromConfiguration(
                    serviceContext.getCoordinationClient(), configuration, AggregationServiceFactory.SERVICE_NAME);
            environment.lifecycle().manage(serviceDiscoveryHelper);
        }
    }
}
