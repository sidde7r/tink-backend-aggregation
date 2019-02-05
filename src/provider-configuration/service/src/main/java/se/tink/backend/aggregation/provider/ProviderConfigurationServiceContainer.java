package se.tink.backend.aggregation.provider;

import com.codahale.metrics.health.HealthCheck;
import com.google.common.collect.ImmutableList;
import io.dropwizard.Application;
import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import se.tink.backend.aggregation.provider.configuration.cli.DebugProviderCommand;
import se.tink.backend.aggregation.provider.configuration.cli.GenerateProviderOnClusterFilesCommand;
import se.tink.backend.aggregation.provider.configuration.cli.ProviderStatusCommand;
import se.tink.backend.aggregation.provider.configuration.config.ProviderModuleFactory;
import se.tink.backend.aggregation.provider.configuration.config.ProviderServiceConfiguration;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;

public class ProviderConfigurationServiceContainer extends Application<ProviderServiceConfiguration> {

    private static final ImmutableList<Command> COMMANDS = ImmutableList.of(
            new DebugProviderCommand(),
            new ProviderStatusCommand(),
            new GenerateProviderOnClusterFilesCommand()
    );

    public static void main(String[] args) throws Exception {
        new ProviderConfigurationServiceContainer().run(args);
    }

    @Override
    public String getName() {
        return "PROVIDER_SERVICE";
    }

    @Override
    public void initialize(Bootstrap<ProviderServiceConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
        COMMANDS.forEach(bootstrap::addCommand);
    }

    @Override
    public void run(ProviderServiceConfiguration configuration, Environment environment)
            throws Exception {

        // Add a dummy health check to avoid an annoying warning on startup.
        environment.healthChecks().register("cache", new HealthCheck() {
            @Override
            protected Result check() throws Exception {
                return Result.healthy();
            }
        });

        DropwizardLifecycleInjectorFactory.build(
                environment.lifecycle(),
                ProviderModuleFactory.build(configuration, environment.jersey()));
    }
}
