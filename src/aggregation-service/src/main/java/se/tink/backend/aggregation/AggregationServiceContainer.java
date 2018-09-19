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
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.common.AbstractServiceContainer;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.auth.ApiTokenAuthorizationHeaderPredicate;
import se.tink.libraries.auth.ContainerAuthorizationResourceFilterFactory;
import se.tink.libraries.auth.YubicoAuthorizationHeaderPredicate;
import se.tink.libraries.cluster.Cluster;
import se.tink.libraries.discovery.ServiceDiscoveryHelper;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.draining.DrainModeTask;

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
        Injector injector = DropwizardLifecycleInjectorFactory.build(
                environment.lifecycle(),
                AggregationModuleFactory.build(configuration, environment));

        ServiceContext serviceContext = injector.getInstance(ServiceContext.class);
        environment.admin().addTask(injector.getInstance(DrainModeTask.class));
        buildContainer(configuration, environment, serviceContext, injector);
    }

    private void buildContainer(ServiceConfiguration configuration, Environment environment,
            ServiceContext serviceContext, Injector injector) {
        AgentWorker agentWorker = injector.getInstance(AgentWorker.class);

        final AggregationServiceResource aggregationServiceResource = new AggregationServiceResource(serviceContext,
                injector.getInstance(MetricRegistry.class),
                new AggregationControllerAggregationClient(
                        configuration.getEndpoints().getAggregationcontroller(),
                        serviceContext.getCoordinationClient()),
                        agentWorker);

        environment.lifecycle().manage(agentWorker);

        CreditSafeServiceResource creditSafeServiceResource = new CreditSafeServiceResource(serviceContext);

        InProcessAggregationServiceFactory inProcessAggregationServiceFactory = (InProcessAggregationServiceFactory)
                serviceContext.getAggregationServiceFactory();

        inProcessAggregationServiceFactory.setAggregationService(aggregationServiceResource);
        inProcessAggregationServiceFactory.setCreditSafeService(creditSafeServiceResource);

        environment.jersey().register(inProcessAggregationServiceFactory.getAggregationService());
    }
}
