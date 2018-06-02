package se.tink.backend.webhook;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Module;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import se.tink.backend.webhook.configuration.WebhookConfiguration;
import se.tink.backend.webhook.configuration.WebhookModuleFactory;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;

public class WebhookServiceContainer extends Application<WebhookConfiguration> {

    public static final String SERVICE_NAME = "webhook";

    public static void main(String[] args) throws Exception {
        new WebhookServiceContainer().run(args);
    }

    @Override
    public String getName() {
        return SERVICE_NAME;
    }

    @Override
    public void initialize(Bootstrap<WebhookConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
    }

    @Override
    public void run(WebhookConfiguration configuration, Environment environment) throws Exception {
        
        // Add a dummy health check to avoid an annoying warning on startup.
        environment.healthChecks().register("cache", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

        Iterable<Module> modules = WebhookModuleFactory.build(configuration, environment.jersey());
        DropwizardLifecycleInjectorFactory.build(environment.lifecycle(), modules);
    }
}
