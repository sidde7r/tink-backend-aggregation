package se.tink.backend.system.cli.debug.provider;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.assertj.core.util.Strings;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Provider;
import se.tink.backend.utils.LogUtils;

public class ProviderStatusesFetcher {
    private static final LogUtils LOGGER = new LogUtils(ProviderStatusesFetcher.class);

    private final ProviderRepository providerRepository;
    private final String market;

    public ProviderStatusesFetcher(ProviderRepository providerRepository, String market) {
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

    private List<Provider> getProviders() {
        if (!Strings.isNullOrEmpty(market)) {
            List<Provider> providers = providerRepository.findProvidersByMarket(market);
            if (providers.isEmpty()) {
                LOGGER.warn(String.format("Did not find any providers for supplied market '%s'", market));
            }
            return providers;
        }

        return providerRepository.findAll();
    }
}
