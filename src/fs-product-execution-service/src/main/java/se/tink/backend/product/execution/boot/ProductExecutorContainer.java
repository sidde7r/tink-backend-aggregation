package se.tink.backend.product.execution.boot;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import se.tink.backend.product.execution.configuration.ProductExecutorConfiguration;
import se.tink.backend.product.execution.configuration.ProductExecutorModuleFactory;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;

public class ProductExecutorContainer extends Application<ProductExecutorConfiguration> {

    public static void main(String[] args) throws Exception {
        new ProductExecutorContainer().run(args);
    }

    @Override
    public void initialize(Bootstrap<ProductExecutorConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
    }


    @Override
    public void run(ProductExecutorConfiguration configuration, Environment environment)
            throws Exception {

        // Add a dummy health check to avoid an annoying warning on startup.
        environment.healthChecks().register("cache", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

        Iterable<Module> modules = ProductExecutorModuleFactory.build(configuration, environment.jersey());
        Injector injector = DropwizardLifecycleInjectorFactory.build(environment.lifecycle(), modules);


    }
}