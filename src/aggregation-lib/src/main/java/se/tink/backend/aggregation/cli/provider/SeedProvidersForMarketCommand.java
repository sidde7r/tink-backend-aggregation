package se.tink.backend.aggregation.cli.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Injector;
import io.dropwizard.setup.Bootstrap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import net.sourceforge.argparse4j.inf.Namespace;
import se.tink.backend.aggregation.cli.AggregationServiceContextCommand;
import se.tink.backend.common.config.ServiceConfiguration;
import se.tink.backend.common.repository.mysql.aggregation.providerconfiguration.ProviderConfigurationRepository;
import se.tink.backend.core.ProviderConfiguration;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.aggregation.log.AggregationLogger;

public class SeedProvidersForMarketCommand extends AggregationServiceContextCommand<ServiceConfiguration> {
    private static final AggregationLogger log = new AggregationLogger(SeedProvidersForMarketCommand.class);
    private static ObjectMapper mapper = new ObjectMapper();
    private ProviderConfigurationRepository providerRepository;
    private final double DEFAULT_INITIAL_REFRESH_FREQUENCY = 1.0;
    private final double DEFAULT_INITIAL_REFRESH_FREQUENCY_FACTOR = 1.0;

    public SeedProvidersForMarketCommand() {
        super("seed-providers-for-market", "Seed database with providers for given market, has optional providerName"
                + "argument for seeding/updating only the specified provider.");
    }

    private void seedProviders(String providerName, String market) throws IOException {
        String providersFilePath = "data/seeding/providers-" + escapeMarket(market).toLowerCase() + ".json";
        File providersFile = new File(providersFilePath);
        ProviderConfigModel providerConfig = mapper.readValue(providersFile, ProviderConfigModel.class);

        String marketInProvidersConfig = providerConfig.getMarket();

        if (!Objects.equal(marketInProvidersConfig.toLowerCase(), market.toLowerCase())) {
            throw new IllegalStateException(String.format(
                    "Supplied market ( %s ) does not match market in the providers config file ( %s ).",
                    market.toLowerCase(), marketInProvidersConfig.toLowerCase()));
        }

        String currencyInProvidersConfig = providerConfig.getCurrency();
        List<ProviderConfiguration> providers = providerConfig.getProviders();

        if (Strings.isNullOrEmpty(providerName)) {
            // No providerName argument supplied, seed all providers in market's providers config file.
            seedAllProvidersInConfigFile(marketInProvidersConfig, currencyInProvidersConfig, providers,
                    providersFile.getName());
        } else {
            // providerName argument supplied, find provider in market's providers config file and seed it.
            seedProviderByProviderName(providerName, marketInProvidersConfig, currencyInProvidersConfig,
                    providers, providersFile.getName());
        }
    }

    private String escapeMarket(String market) {
        return market.replaceAll("[^a-zA-Z]", "");
    }

    private void seedAllProvidersInConfigFile(String marketInProvidersConfig, String currencyInProvidersConfig,
            List<ProviderConfiguration> providers, String providersFileName) {
        log.info(String.format("Seeding all providers for %s (from %s)",
                marketInProvidersConfig, providersFileName));

        providers.forEach(provider -> seedProvider(provider, marketInProvidersConfig, currencyInProvidersConfig));

        log.info(String.format("Done seeding %d providers for %s", providers.size(), marketInProvidersConfig));
    }

    private void seedProviderByProviderName(String providerName, String marketInProvidersConfig,
            String currencyInProvidersConfig, List<ProviderConfiguration> providers, String providersFileName) {
        Optional<ProviderConfiguration> providerInConfig = providers.stream()
                .filter(provider -> Objects.equal(provider.getName(), providerName))
                .findFirst();

        if (!providerInConfig.isPresent()) {
            throw new IllegalStateException(String.format(
                    "Supplied provider, %s, does not exist in the %s providers config file.",
                    providerName, marketInProvidersConfig));
        }

        log.info(String.format("Seeding provider '%s' for %s (from %s)",
                providerName, marketInProvidersConfig, providersFileName));

        seedProvider(providerInConfig.get(), marketInProvidersConfig, currencyInProvidersConfig);

        log.info(String.format("Done seeding provider '%s'", providerName));
    }

    private void seedProvider(ProviderConfiguration providerToSeed, String market, String currency) {
        providerToSeed.setCurrency(currency);
        providerToSeed.setMarket(market);
        providerToSeed.setRefreshFrequency(DEFAULT_INITIAL_REFRESH_FREQUENCY);
        providerToSeed.setRefreshFrequencyFactor(DEFAULT_INITIAL_REFRESH_FREQUENCY_FACTOR);

        ProviderConfiguration checkedProviderToSeed = checkPotentiallyModifiedValuesInDatabase(providerToSeed);
        providerRepository.save(checkedProviderToSeed);
    }

    private ProviderConfiguration checkPotentiallyModifiedValuesInDatabase(ProviderConfiguration providerToSeed) {
        ProviderConfiguration providerInDatabase = providerRepository.findOne(providerToSeed.getName());

        if (providerInDatabase == null) {
            return providerToSeed;
        }

        ProviderStatuses statusInDatabase = providerInDatabase.getStatus();
        double refreshFrequencyInDatabase = providerInDatabase.getRefreshFrequency();
        double refreshFrequencyFactorInDatabase = providerInDatabase.getRefreshFrequencyFactor();

        if (!Objects.equal(statusInDatabase, providerToSeed.getStatus())) {
            log.warn(String.format("Status in database for provider '%s', does not match status in the "
                            + "providers config file. Status in the database ( '%s' ) has not been updated.",
                    providerToSeed.getName(), statusInDatabase));
            providerToSeed.setStatus(statusInDatabase);
        }

        if (!Objects.equal(refreshFrequencyInDatabase, providerToSeed.getRefreshFrequency())) {
            log.warn(String.format("Refresh frequency in database for provider '%s' does not match frequency in"
                            + " the providers config file. Frequency in the database ( %s ) has not been updated.",
                    providerToSeed.getName(), refreshFrequencyInDatabase));
            providerToSeed.setRefreshFrequency(refreshFrequencyInDatabase);
        }

        if (!Objects.equal(refreshFrequencyFactorInDatabase, providerToSeed.getRefreshFrequencyFactor())) {
            log.warn(String.format("Refresh frequency factor in database for provider '%s' does not match "
                            + "factor in the providers config file. Factor in the database ( %s ) "
                            + "has not been updated.",
                    providerToSeed.getName(), refreshFrequencyFactorInDatabase));
            providerToSeed.setRefreshFrequencyFactor(refreshFrequencyFactorInDatabase);
        }

        return providerToSeed;
    }

    @Override
    protected void run(Bootstrap<ServiceConfiguration> bootstrap, Namespace namespace,
            ServiceConfiguration configuration, Injector injector) throws Exception {
        final String providerName = System.getProperty("providerName");
        final String market = System.getProperty("market");
        Preconditions.checkNotNull(market, "market can't be null.");

        providerRepository = injector.getInstance(ProviderConfigurationRepository.class);
        try {
            seedProviders(providerName, market);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
