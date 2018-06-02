package se.tink.backend.insights;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Injector;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import se.tink.backend.insights.configuration.ActionableInsightsConfiguration;
import se.tink.backend.insights.configuration.InsightsModuleFactory;
import se.tink.backend.main.auth.JerseyAuthenticationProvider;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;

public class ActionableInsightsContainer extends Application<ActionableInsightsConfiguration> {

    public static void main(String[] args) throws Exception {
        new ActionableInsightsContainer().run(args);
    }

    @Override
    public void initialize(Bootstrap<ActionableInsightsConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
    }

    @Override
    public void run(ActionableInsightsConfiguration configuration, Environment environment)
            throws Exception {

        // Add a dummy health check to avoid an annoying warning on startup.
        environment.healthChecks().register("cache", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

        Iterable<Module> modules = InsightsModuleFactory.build(configuration, environment.jersey());
        Injector injector = DropwizardLifecycleInjectorFactory.build(environment.lifecycle(), modules);

        environment.jersey().register(injector.getInstance(JerseyAuthenticationProvider.class));

    }
}
