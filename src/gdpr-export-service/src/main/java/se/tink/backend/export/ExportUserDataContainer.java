package se.tink.backend.export;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import se.tink.backend.export.configuration.ExportUserDataConfiguration;
import se.tink.backend.export.configuration.ExportUserDataModuleFactory;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;


public class ExportUserDataContainer extends Application<ExportUserDataConfiguration> {

    public static void main(String[] args) throws Exception {
        new ExportUserDataContainer().run(args);
    }

    @Override
    public void initialize(Bootstrap<ExportUserDataConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
    }

    @Override
    public void run(ExportUserDataConfiguration configuration, Environment environment)
            throws Exception {

        // Add a dummy health check to avoid an annoying warning on startup.
        environment.healthChecks().register("cache", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

        Iterable<Module> modules = ExportUserDataModuleFactory
                .build(configuration, environment.jersey());
        DropwizardLifecycleInjectorFactory.build(environment.lifecycle(), modules);
    }
}
