package se.tink.backend.main.controllers;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import se.tink.backend.common.dao.ProviderDao;
import se.tink.backend.common.repository.mysql.main.CredentialsRepository;
import se.tink.backend.core.Credentials;
import se.tink.backend.core.Field;
import se.tink.backend.core.Provider;
import se.tink.backend.core.ProviderStatuses;
import se.tink.backend.utils.ProviderImageMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static se.tink.backend.core.ProviderStatuses.DISABLED;
import static se.tink.backend.core.ProviderStatuses.ENABLED;
import static se.tink.backend.core.ProviderStatuses.OBSOLETE;

@RunWith(JUnitParamsRunner.class)
public class ProviderServiceControllerTest {
    @Rule public MockitoRule rule = MockitoJUnit.rule();

    @InjectMocks ProviderServiceController providerServiceController;
    @Mock CredentialsRepository credentialsRepository;
    @Mock Supplier<ProviderImageMap> providerImageMapSupplier;
    @Mock ProviderImageMap providerImageMap;
    @Mock ProviderDao providerDao;

    @Before
    public void setUp() {
        mockDefaultData();
    }

    private void mockDefaultData() {
        when(providerDao.getProviders()).thenReturn(getDefaultProviders());
        when(providerImageMapSupplier.get()).thenReturn(providerImageMap);
    }

    private ImmutableList<Provider> getDefaultProviders() {
        return ImmutableList.of(createProvider("SE_ENABLED", "SE", ENABLED),
                createProvider("SE_MIXCASE_ENABLED", "Se", ENABLED),
                createProvider("SE_LOWERCASE_ENABLED", "se", ENABLED),
                createProvider("SE_DISABLED", "SE", DISABLED),
                createProvider("FR_ENABLED", "FR", ENABLED),
                createProvider("FR_DISABLED", "fr", DISABLED));
    }

    @Test
    @Parameters({ "SE, 4", "se, 4", "Se, 4", "Fr, 2", "UK, 0" })
    public void filterAllProvidersByMarketForUser(String market, int expectedProvidersSize) {

        when(credentialsRepository.findAllByUserId(eq("userId")))
                .thenReturn(ImmutableList.of(createCredentials("FR_DISABLED"), createCredentials("SE_DISABLED")));

        List<Provider> providers = providerServiceController.list("userId", market);

        assertEquals(expectedProvidersSize, providers.size());
        for (Provider provider : providers) {
            assertEquals(market.toLowerCase(), provider.getMarket().toLowerCase());
        }
    }

    @Test
    @Parameters({ "SE, 3", "se, 3", "Se, 3", "Fr, 1", "UK, 0" })
    public void filterNotDisabledProvidersByMarketForUser(String market, int expectedProvidersSize) {
        when(credentialsRepository.findAllByUserId(eq("userId")))
                .thenReturn(ImmutableList.of(createCredentials("Unknown")));

        List<Provider> providers = providerServiceController.list("userId", market);

        assertEquals(expectedProvidersSize, providers.size());
        for (Provider provider : providers) {
            assertEquals(market.toLowerCase(), provider.getMarket().toLowerCase());
            assertNotEquals(DISABLED, provider.getStatus());
        }
    }

    @Test
    public void returnCleanedProviderForUser() {
        when(credentialsRepository.findAllByUserId(eq("userId")))
                .thenReturn(ImmutableList.of(createCredentials("Unknown")));

        List<Provider> providers = providerServiceController.list("userId", "SE");

        assertFalse(providers.isEmpty());

        for (Provider provider : providers) {
            assertTrue(isCleanProvider(provider));
        }

    }

    @Test
    public void populateImageToProvidersForUser() {
        when(credentialsRepository.findAllByUserId(eq("userId")))
                .thenReturn(ImmutableList.of(createCredentials("Unknown")));

        List<Provider> providers = providerServiceController.list("userId", "SE");

        assertFalse(providers.isEmpty());

        verify(providerImageMap, times(providers.size())).populateImagesForProvider(any());
    }

    private Object[] parametersForListProvidersByMarket() {
        return new Object[] {
                new Object[] { Optional.empty(), "SE", 0 },
                new Object[] { Optional.of("Unknown"), "SE", 0 },
                new Object[] { Optional.of("SE_ENABLED,SE_DISABLED"), "fr", 0 },
                new Object[] { Optional.of("FR_DISABLED"), "fr", 1 },
                new Object[] { Optional.of("SE_ENABLED,SE_DISABLED,FR_ENABLED"), "SE", 2 },
        };
    }

    @Test
    @Parameters(method = "parametersForListProvidersByMarket")
    public void listProvidersByMarket(Optional<String> stringProviders, String market, int expectedProvidersSize) {
        List<Provider> providers = providerServiceController.listByMarket(stringProviders, market);

        assertEquals(expectedProvidersSize, providers.size());
    }

    @Test
    public void returnCleanedProviderForListByMarket() {
        List<Provider> providers = providerServiceController.listByMarket(Optional.of("SE_ENABLED"), "SE");

        assertFalse(providers.isEmpty());

        for (Provider provider : providers) {
            assertTrue(isCleanProvider(provider));
        }

    }

    @Test
    public void populateImageToProvidersForListByMarket() {
        List<Provider> providers = providerServiceController.listByMarket(Optional.of("SE_ENABLED"), "SE");

        assertFalse(providers.isEmpty());

        verify(providerImageMap, times(providers.size())).populateImagesForProvider(any());
    }

    private boolean isCleanProvider(Provider provider) {

        if (provider.getClassName() != null) {
            return false;
        }

        if (provider.getPayload() != null) {
            return false;
        }

        for (Field field : provider.getFields()) {
            if (!field.isExposed()) {
                return false;
            }
        }

        return !Objects.equals(provider.getStatus(), OBSOLETE);
    }

    private Credentials createCredentials(String providerName) {
        Credentials credentials = new Credentials();
        credentials.setProviderName(providerName);
        return credentials;
    }

    private Provider createProvider(String name, String market, ProviderStatuses status) {
        Provider provider = new Provider();
        provider.setName(name);
        provider.setMarket(market);
        provider.setStatus(status);
        return provider;
    }

}
