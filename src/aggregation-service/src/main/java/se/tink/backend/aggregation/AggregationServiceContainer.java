package se.tink.backend.aggregation;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import se.tink.backend.aggregation.guice.configuration.AggregationModuleFactory;
import se.tink.backend.aggregation.workers.AgentWorker;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.libraries.draining.DrainModeTask;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;

public class AggregationServiceContainer extends Application<ServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new AggregationServiceContainer().run(args);
    }

    @Override
    public void initialize(Bootstrap<ServiceConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
    }

    @Override
    public void run(ServiceConfiguration serviceConfiguration, Environment environment) throws Exception {
        // Add a dummy health check to avoid an annoying warning on startup.
        environment.healthChecks().register("cache", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

        Injector injector = DropwizardLifecycleInjectorFactory.build(
                environment.lifecycle(), AggregationModuleFactory.build(serviceConfiguration, environment));

        environment.admin().addTask(injector.getInstance(DrainModeTask.class));
        environment.lifecycle().manage(injector.getInstance(AgentWorker.class));
    }
}
