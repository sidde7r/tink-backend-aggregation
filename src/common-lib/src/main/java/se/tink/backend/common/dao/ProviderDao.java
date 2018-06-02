package se.tink.backend.common.dao;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Provider;
import se.tink.backend.guice.configuration.ProviderCacheConfiguration;

public class ProviderDao {

    private final Supplier<ProviderCache> providerCache;

    @Inject
    public ProviderDao(@Named("isProvidersOnAggregation") boolean isProvidersOnAggregation,
            ProviderRepository providerRepository, AggregationControllerCommonClient aggregationControllerClient,
            ProviderCacheConfiguration providerCacheConfiguration) {
        providerCache = Suppliers.memoizeWithExpiration(
                createProviderCacheSupplier(isProvidersOnAggregation, providerRepository, aggregationControllerClient),
                providerCacheConfiguration.getDuration(), providerCacheConfiguration.getTimeUnit());
    }

    private static Supplier<ProviderCache> createProviderCacheSupplier(
            boolean isProvidersOnAggregation, final ProviderRepository providerRepository,
            AggregationControllerCommonClient aggregationControllerClient) {
        if (isProvidersOnAggregation) {
            return () -> {
                List<Provider> providersInDb = aggregationControllerClient.listProviders();
                return new ProviderCache(providersInDb);
            };
        } else {
            return () -> {
                List<Provider> providersInDb = providerRepository.findAll();
                return new ProviderCache(providersInDb);
            };
        }
    }

    /**
     * Uses provider cache through supplier
     */
    public ImmutableList<Provider> getProviders() {
        return providerCache.get().getProviders();
    }

    /**
     * Uses provider cache through supplier
     */
    public ImmutableMap<String, Provider> getProvidersByName() {
        return providerCache.get().getProvidersByName();
    }

    private static class ProviderCache {

        private final ImmutableList<Provider> providers;
        private final ImmutableMap<String, Provider> providersByName;

        private ProviderCache(List<Provider> providersInDb) {
            providers = ImmutableList.copyOf(providersInDb.stream().sorted(
                    Comparator.comparing(Provider::getDisplayName))
                    .collect(Collectors.toList()));
            providersByName = FluentIterable.from(providers).uniqueIndex(Provider::getName);
        }

        private ImmutableList<Provider> getProviders() {
            return providers;
        }

        private ImmutableMap<String, Provider> getProvidersByName() {
            return providersByName;
        }
    }
}
