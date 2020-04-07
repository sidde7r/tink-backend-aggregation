package se.tink.backend.aggregation;

import com.codahale.metrics.health.HealthCheck;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import se.tink.backend.aggregation.cli.AddClientConfigurationsCommand;
import se.tink.backend.aggregation.configuration.ConfigurationValidator;
import se.tink.backend.aggregation.configuration.DevelopmentConfigurationSeeder;
import se.tink.backend.aggregation.configuration.guice.modules.AggregationModuleFactory;
import se.tink.backend.aggregation.configuration.models.AggregationServiceConfiguration;
import se.tink.backend.aggregation.storage.database.daos.CryptoConfigurationDao;
import se.tink.backend.aggregation.workers.worker.AgentWorker;
import se.tink.backend.integration.agent_data_availability_tracker.client.AgentDataAvailabilityTrackerClient;
import se.tink.backend.integration.tpp_secrets_service.client.ManagedTppSecretsServiceClient;
import se.tink.libraries.draining.DrainModeTask;
import se.tink.libraries.dropwizard.DropwizardLifecycleInjectorFactory;
import se.tink.libraries.dropwizard.DropwizardObjectMapperConfigurator;

public class AggregationServiceContainer extends Application<AggregationServiceConfiguration> {

    public static void main(String[] args) throws Exception {
        new AggregationServiceContainer().run(args);
    }

    @Override
    public void initialize(Bootstrap<AggregationServiceConfiguration> bootstrap) {
        DropwizardObjectMapperConfigurator.doNotFailOnUnknownProperties(bootstrap);
        bootstrap.addCommand(new AddClientConfigurationsCommand());
    }

    @Override
    public void run(
            AggregationServiceConfiguration aggregationServiceConfiguration,
            Environment environment)
            throws Exception {
        // Add a dummy health check to avoid an annoying warning on startup.
        environment
                .healthChecks()
                .register(
                        "cache",
                        new HealthCheck() {
                            @Override
                            protected Result check() throws Exception {
                                return Result.healthy();
                            }
                        });

        Injector injector = generateInjector(aggregationServiceConfiguration, environment);

        setupCryptoConfiguration(injector, aggregationServiceConfiguration.isDevelopmentMode());

        // Validate the configurations on start up
        ConfigurationValidator validator = injector.getInstance(ConfigurationValidator.class);
        validator.validate();

        environment.admin().addTask(injector.getInstance(DrainModeTask.class));

        environment.lifecycle().manage(injector.getInstance(ManagedTppSecretsServiceClient.class));
        environment.lifecycle().manage(injector.getInstance(AgentWorker.class));
        environment
                .lifecycle()
                .manage(injector.getInstance(AgentDataAvailabilityTrackerClient.class));
    }

    /**
     * Ensures the setup is done in the correct order.
     *
     * <p>In development mode, we have to seed the database before {@link CryptoConfigurationDao}
     * creates a mapping of them.
     */
    private void setupCryptoConfiguration(Injector injector, boolean isDevelopmentMode) {
        if (isDevelopmentMode) {
            injector.getInstance(DevelopmentConfigurationSeeder.class).seedCryptoConfiguration();
        }

        injector.getInstance(CryptoConfigurationDao.class).populateCryptoConfiguration();
    }

    public Injector generateInjector(
            AggregationServiceConfiguration aggregationServiceConfiguration,
            Environment environment) {
        return DropwizardLifecycleInjectorFactory.build(
                environment.lifecycle(),
                AggregationModuleFactory.build(aggregationServiceConfiguration, environment));
    }
}
