package se.tink.backend.aggregation.provider.configuration.cli;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfiguration;
import se.tink.backend.aggregation.provider.configuration.core.ProviderConfigurationDAO;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// FIXME: move business logic to controller instead of using DAO directly
public class ProviderStatusesFetcher {
    private static final Logger log = LoggerFactory.getLogger(
            ProviderStatusesFetcher.class);

    private final String market;
    private final ProviderConfigurationDAO providerConfigurationDAO;

    public ProviderStatusesFetcher(ProviderConfigurationDAO providerConfigurationDAO, String market) {
        this.providerConfigurationDAO = providerConfigurationDAO;
        this.market = market;
    }

    public void fetch(Consumer<List<Map<String, String>>> consumer) {
        log.info("Showing list of provider statuses.");

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
            List<ProviderConfiguration> providers = providerConfigurationDAO.findAllByMarket(market);
            if (providers.isEmpty()) {
                log.warn(String.format("Did not find any providers for supplied market '%s'", market));
            }
            return providers;
        }

        return providerConfigurationDAO.findAll();
    }
}
