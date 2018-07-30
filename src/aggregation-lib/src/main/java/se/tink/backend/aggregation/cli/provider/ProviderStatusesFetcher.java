package se.tink.backend.aggregation.cli.provider;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.assertj.core.util.Strings;
import se.tink.backend.common.repository.mysql.aggregation.providerconfiguration.ProviderConfigurationRepository;
import se.tink.backend.core.ProviderConfiguration;
import se.tink.backend.aggregation.log.AggregationLogger;

public class ProviderStatusesFetcher {
    private static final AggregationLogger LOGGER = new AggregationLogger(
            ProviderStatusesFetcher.class);

    private final ProviderConfigurationRepository providerRepository;
    private final String market;

    public ProviderStatusesFetcher(ProviderConfigurationRepository providerRepository, String market) {
        this.providerRepository = providerRepository;
        this.market = market;
    }

    public void fetch(Consumer<List<Map<String, String>>> consumer) {
        LOGGER.info("Showing list of provider statuses.");

        consumer.accept(
                getProviders().stream()
                        .map(provider ->
                                ImmutableMap.of(
                                        "Provider name", provider.getName(),
                                        "Provider status", provider.getStatus().name()
                                )
                        )
                        .collect(Collectors.toList())
        );
    }

    private List<ProviderConfiguration> getProviders() {
        if (!Strings.isNullOrEmpty(market)) {
            List<ProviderConfiguration> providers = providerRepository.findAllByMarket(market);
            if (providers.isEmpty()) {
                LOGGER.warn(String.format("Did not find any providers for supplied market '%s'", market));
            }
            return providers;
        }

        return providerRepository.findAll();
    }
}
