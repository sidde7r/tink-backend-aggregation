package se.tink.backend.aggregation;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.ws.rs.Path;
import se.tink.backend.aggregation.aggregationcontroller.AggregationControllerAggregationClient;
import se.tink.backend.aggregation.client.AggregationServiceFactory;
import se.tink.backend.aggregation.client.InProcessAggregationServiceFactory;
import se.tink.backend.aggregation.guice.configuration.AggregationModuleFactory;
import se.tink.backend.aggregation.resources.AggregationServiceResource;
import se.tink.backend.aggregation.resources.CreditSafeServiceResource;
import se.tink.backend.aggregation.storage.AgentDebugStorageHandler;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.common.AbstractServiceContainer;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;
import se.tink.libraries.metrics.MetricRegistry;
import se.tink.libraries.draining.DrainModeTask;

@Path("/aggregation")
public class AggregationServiceContainer extends AbstractServiceContainer {

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
        AgentDebugStorageHandler agentDebugStorageHandler = injector.getInstance(AgentDebugStorageHandler.class);

        final AggregationServiceResource aggregationServiceResource = new AggregationServiceResource(serviceContext,
                injector.getInstance(MetricRegistry.class),
                new AggregationControllerAggregationClient(
                        configuration.getEndpoints().getAggregationcontroller(),
                        serviceContext.getCoordinationClient()),
                        agentWorker,
                agentDebugStorageHandler);

        environment.lifecycle().manage(agentWorker);

        CreditSafeServiceResource creditSafeServiceResource = new CreditSafeServiceResource(serviceContext);

        InProcessAggregationServiceFactory inProcessAggregationServiceFactory = (InProcessAggregationServiceFactory)
                serviceContext.getAggregationServiceFactory();

        inProcessAggregationServiceFactory.setAggregationService(aggregationServiceResource);
        inProcessAggregationServiceFactory.setCreditSafeService(creditSafeServiceResource);

        environment.jersey().register(inProcessAggregationServiceFactory.getAggregationService());
        environment.jersey().register(inProcessAggregationServiceFactory.getCreditSafeService());
    }
}
