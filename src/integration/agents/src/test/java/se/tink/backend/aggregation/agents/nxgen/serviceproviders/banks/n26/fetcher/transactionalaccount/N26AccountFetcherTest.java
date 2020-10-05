package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactionalaccount;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactionalaccount.N26AccountFetcherTestData.fetchAccountsResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactionalaccount.N26AccountFetcherTestData.fetchSavingsAccountsResponse;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.transactionalaccount.N26AccountFetcherTestData.fetchSpaceSavingsAccountsResponse;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.N26ApiClient;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class N26AccountFetcherTest {

    private N26AccountFetcher fetcher;

    @Before
    public void before() {
        N26ApiClient client = mock(N26ApiClient.class);

        when(client.fetchAccounts()).thenReturn(fetchAccountsResponse());
        when(client.fetchSavingsAccounts()).thenReturn(fetchSavingsAccountsResponse());
        when(client.fetchSavingsSpaceAccounts()).thenReturn(fetchSpaceSavingsAccountsResponse());

        fetcher = new N26AccountFetcher(client);
    }

    @Test
    public void shouldFetchAccounts() {
        Collection<TransactionalAccount> transactionalAccounts = fetcher.fetchAccounts();
        assertThat(transactionalAccounts).hasSize(4);
        TransactionalAccount acc1 =
                transactionalAccounts.stream()
                        .filter(a -> "N26 Bank".equals(a.getName()))
                        .findAny()
                        .orElseThrow(IllegalStateException::new);
        TransactionalAccount acc2 =
                transactionalAccounts.stream()
                        .filter(a -> "Savings".equals(a.getName()))
                        .findAny()
                        .orElseThrow(IllegalStateException::new);
        TransactionalAccount acc3 =
                transactionalAccounts.stream()
                        .filter(a -> "Kapitan Bomba savings".equals(a.getName()))
                        .findAny()
                        .orElseThrow(IllegalStateException::new);
        TransactionalAccount acc4 =
                transactionalAccounts.stream()
                        .filter(a -> "Chorazy Torpeda savings".equals(a.getName()))
                        .findAny()
                        .orElseThrow(IllegalStateException::new);

        assertThat(acc1).hasFieldOrPropertyWithValue("accountNumber", "DE95100110016601026293");
        assertThat(acc1.getExactBalance().getExactValue()).isEqualByComparingTo("100.0");
        assertThat(acc1.getExactBalance().getCurrencyCode()).isEqualTo("EUR");

        assertThat(acc2)
                .hasFieldOrPropertyWithValue(
                        "accountNumber", "08e298f5-5de9-4d5c-8728-8aa07a0b663f");
        assertThat(acc2.getExactBalance().getExactValue()).isEqualByComparingTo("1.99");
        assertThat(acc2.getExactBalance().getCurrencyCode()).isEqualTo("EUR");

        assertThat(acc3)
                .hasFieldOrPropertyWithValue(
                        "accountNumber", "013c8f14-0acd-4e5b-93b1-c4b2c213b7a6");
        assertThat(acc3.getExactBalance().getExactValue()).isEqualByComparingTo("98.99");
        assertThat(acc3.getExactBalance().getCurrencyCode()).isEqualTo("EUR");

        assertThat(acc4)
                .hasFieldOrPropertyWithValue(
                        "accountNumber", "355fe9f2-f503-4843-ae05-023a1e89096c");
        assertThat(acc4.getExactBalance().getExactValue()).isEqualByComparingTo("1.00");
        assertThat(acc4.getExactBalance().getCurrencyCode()).isEqualTo("EUR");
    }
}
