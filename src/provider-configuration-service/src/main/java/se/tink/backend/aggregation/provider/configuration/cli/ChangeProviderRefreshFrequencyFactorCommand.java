package se.tink.backend.aggregation.provider.configuration.cli;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.aggregation.provider.configuration.repositories.mysql.ProviderConfigurationRepository;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.core.ProviderConfiguration;

import java.util.List;
import java.util.Optional;

public class ChangeProviderRefreshFrequencyFactorCommand extends
        ProviderConfigurationCommand<ServiceConfiguration> {
    private static final String REFRESH_FREQUENCY_INTERVAL_ERROR_MESSAGE = "'refreshFrequencyFactor' must be 0 <= x <= 1.";

    public ChangeProviderRefreshFrequencyFactorCommand() {
        super("change-refresh-frequency-factor",
                "Change refresh frequency factor for one, or all providers.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector) throws Exception {

        // Input handling

        final Optional<String> providerName = Optional.ofNullable(System.getProperty("providerName"));
        final Double refreshFrequency = getRefreshFrequencyFromSystemProperty();

        // Print parameters.

        System.out.println("'providerName' to search for is: " + providerName);
        System.out.println("New 'refreshFrequencyFactor' is: " + refreshFrequency);

        // Make the actual change.

        ProviderConfigurationRepository providerConfigurationRepository = injector.getInstance(
                ProviderConfigurationRepository.class);

        if (providerName.isPresent()) {
            ProviderConfiguration providerConfiguration = providerConfigurationRepository.findByName(providerName.get());
            Preconditions.checkNotNull(providerConfiguration,
                    String.format("Could not find provider with the given name: %s", providerName.get()));

            providerConfiguration.setRefreshFrequency(refreshFrequency);

            providerConfigurationRepository.save(providerConfiguration);

            System.out.println("Provider modified.");
        } else {
            List<ProviderConfiguration> providers = providerConfigurationRepository.findAll();

            for (ProviderConfiguration provider : providers) {
                provider.setRefreshFrequencyFactor(refreshFrequency);
            }

            providerConfigurationRepository.save(providers);

            System.out.println("Providers modified.");
        }

    }

    private Double getRefreshFrequencyFromSystemProperty() {
        final Double refreshFrequency;
        String refreshFrequencyString = System.getProperty("refreshFrequency");
        refreshFrequency = Double.parseDouble(Preconditions.checkNotNull(refreshFrequencyString,
                "'refreshFrequency' must be given as Java system property."));

        Preconditions.checkArgument(refreshFrequency >= 0, REFRESH_FREQUENCY_INTERVAL_ERROR_MESSAGE);
        Preconditions.checkArgument(refreshFrequency <= 1, REFRESH_FREQUENCY_INTERVAL_ERROR_MESSAGE);
        return refreshFrequency;
    }

}
