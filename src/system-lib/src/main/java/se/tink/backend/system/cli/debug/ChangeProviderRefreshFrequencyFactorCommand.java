package se.tink.backend.system.cli.debug;

import com.google.common.base.Preconditions;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.util.List;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.common.ServiceContext;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Provider;
import se.tink.backend.system.cli.ServiceContextCommand;

public class ChangeProviderRefreshFrequencyFactorCommand extends ServiceContextCommand<ServiceConfiguration> {
    private static final String REFRESH_FREQUENCY_INTERVAL_ERROR_MESSAGE = "'refreshFrequencyFactor' must be 0 <= x <= 1.";

    public ChangeProviderRefreshFrequencyFactorCommand() {
        super("change-refresh-frequency-factor",
                "Change refresh frequency factor for one, or all providers.");
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector, ServiceContext serviceContext) throws Exception {

        // Input handling

        final Optional<String> providerName = Optional.ofNullable(System.getProperty("providerName"));
        final Double refreshFrequency = getRefreshFrequencyFromSystemProperty();

        // Print parameters.

        System.out.println("'providerName' to search for is: " + providerName);
        System.out.println("New 'refreshFrequencyFactor' is: " + refreshFrequency);

        // Make the actual change.

        ProviderRepository providerRepository = serviceContext.getRepository(ProviderRepository.class);

        if (providerName.isPresent()) {
            Provider provider = providerRepository.findByName(providerName.get());
            Preconditions.checkNotNull(provider,
                    String.format("Could not find provider with the given name: %s", providerName.get()));

            provider.setRefreshFrequency(refreshFrequency);

            providerRepository.save(provider);

            System.out.println("Provider modified.");
        } else {
            List<Provider> providers = providerRepository.findAll();

            for (Provider provider : providers) {
                provider.setRefreshFrequencyFactor(refreshFrequency);
            }

            providerRepository.save(providers);

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
