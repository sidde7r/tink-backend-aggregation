package se.tink.backend.aggregation;

import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.ws.rs.Path;
import se.tink.backend.aggregation.guice.configuration.AggregationModuleFactory;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.common.AbstractServiceContainer;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.draining.DrainModeTask;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;

@Path("/aggregation")
public class AggregationServiceContainer extends AbstractServiceContainer {

    public static void main(String[] args) throws Exception {
        new AggregationServiceContainer().run(args);
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void build(ServiceConfiguration configuration, Environment environment) throws Exception {
        Injector injector = DropwizardLifecycleInjectorFactory.build(
                environment.lifecycle(), AggregationModuleFactory.build(configuration, environment));

        environment.admin().addTask(injector.getInstance(DrainModeTask.class));
        environment.lifecycle().manage(injector.getInstance(AgentWorker.class));
    }
}
