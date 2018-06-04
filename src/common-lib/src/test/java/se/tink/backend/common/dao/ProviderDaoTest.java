package se.tink.backend.common.dao;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import se.tink.backend.common.client.AggregationControllerCommonClient;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Provider;
import se.tink.backend.guice.configuration.ProviderCacheConfiguration;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ProviderDaoTest {
    @Test
    public void emptyProvidersTable() {
        ProviderRepository providerRepository = mock(ProviderRepository.class);
        when(providerRepository.findAll()).thenReturn(Collections.<Provider>emptyList());

        ProviderDao providerDao = new ProviderDao(false, providerRepository,
                mock(AggregationControllerCommonClient.class), new ProviderCacheConfiguration(5, TimeUnit.MINUTES));

        assertThat(providerDao.getProviders()).isEmpty();
        assertThat(providerDao.getProvidersByName()).isEmpty();
    }

    @Test
    public void providersAreCached() {
        ProviderDao providerDao = new ProviderDao(false, createProviderRepositoryWithTwoProviders(),
                mock(AggregationControllerCommonClient.class), new ProviderCacheConfiguration(1, TimeUnit.MINUTES));

        // If cached we should get exactly the same object instances
        assertThat(providerDao.getProviders()).isSameAs(providerDao.getProviders());
        assertThat(providerDao.getProvidersByName()).isSameAs(providerDao.getProvidersByName());
    }

    @Test
    public void providerCacheIsInvalidatedAfterExpiration() {
        ProviderDao providerDao = new ProviderDao(false, createProviderRepositoryWithTwoProviders(),
                mock(AggregationControllerCommonClient.class), new ProviderCacheConfiguration(1, TimeUnit.NANOSECONDS));

        ImmutableList<Provider> providers = providerDao.getProviders();
        ImmutableMap<String, Provider> providersByName = providerDao.getProvidersByName();

        Uninterruptibles.sleepUninterruptibly(10, TimeUnit.MILLISECONDS);

        // If cache is expired we should get different object instances
        assertThat(providerDao.getProviders()).isNotSameAs(providers);
        assertThat(providerDao.getProvidersByName()).isNotSameAs(providersByName);
    }

    private ProviderRepository createProviderRepositoryWithTwoProviders() {
        Provider provider1 = new Provider();
        provider1.setName("provider1name");
        provider1.setDisplayName("Provider1DisplayName");

        Provider provider2 = new Provider();
        provider2.setName("provider2name");
        provider2.setDisplayName("Provider2DisplayName");

        return createProviderRepository(provider1, provider2);
    }

    private ProviderRepository createProviderRepository(final Provider... providers) {
        ProviderRepository providerRepository = mock(ProviderRepository.class);
        when(providerRepository.findAll()).thenReturn(Lists.newArrayList(providers));
        return providerRepository;
    }
}
