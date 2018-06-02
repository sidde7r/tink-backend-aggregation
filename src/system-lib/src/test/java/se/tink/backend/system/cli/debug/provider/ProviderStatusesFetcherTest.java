package se.tink.backend.system.cli.debug.provider;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.junit.Test;
import se.tink.backend.common.repository.mysql.main.ProviderRepository;
import se.tink.backend.core.Provider;
import static java.util.Arrays.asList;
import static org.hamcrest.core.IsCollectionContaining.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.core.ProviderStatuses.ENABLED;

public class ProviderStatusesFetcherTest {

    @Test
    public void canPrintAll() throws Exception {
        List<Provider> providers = getListOfProviders();

        ProviderRepository repository = mock(ProviderRepository.class);
        when(repository.findAll()).thenReturn(providers);

        Consumer<List<Map<String, String>>> consumer = rows -> {
            assertEquals(2, rows.size());
            Map<String, String> firstRow = rows.get(0);
            Map<String, String> secondRow = rows.get(1);

            Collection<String> values = firstRow.values();
            assertEquals(2, values.size());
            assertThat(values, hasItems("bankSE", ENABLED.toString()));

            values = secondRow.values();
            assertEquals(2, values.size());
            assertThat(values, hasItems("bankNO", ENABLED.toString()));
        };

        new ProviderStatusesFetcher(repository, null).fetch(consumer);
    }

    @Test
    public void canPrintForMarket() {
        List<Provider> providers = getListOfProviders();

        ProviderRepository repository = mock(ProviderRepository.class);
        when(repository.findProvidersByMarket("NO")).thenReturn(Collections.singletonList(providers.get(1)));

        Consumer<List<Map<String, String>>> consumer = rows -> {
            assertEquals(1, rows.size());
            Map<String, String> firstRow = rows.get(0);

            Collection<String> values = firstRow.values();
            assertEquals(2, values.size());
            assertThat(values, hasItems("bankNO", ENABLED.toString()));
        };

        new ProviderStatusesFetcher(repository, "NO").fetch(consumer);
    }

    private List<Provider> getListOfProviders() {
        Provider providerSE = new Provider();
        providerSE.setName("bankSE");
        providerSE.setStatus(ENABLED);
        providerSE.setMarket("SE");

        Provider providerNO = new Provider();
        providerNO.setName("bankNO");
        providerNO.setStatus(ENABLED);
        providerNO.setMarket("NO");

        return asList(providerSE, providerNO);
    }
}
