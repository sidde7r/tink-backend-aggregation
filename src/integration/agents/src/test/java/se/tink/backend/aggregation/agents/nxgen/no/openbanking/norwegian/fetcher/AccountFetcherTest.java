package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.data.NorwegianFetcherTestData;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.transactionalaccount.NorwegianTransactionalAccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class AccountFetcherTest {

    private NorwegianTransactionalAccountFetcher accountFetcher;

    @Before
    public void init() {
        NorwegianApiClient client = mock(NorwegianApiClient.class);
        when(client.fetchAccounts()).thenReturn(NorwegianFetcherTestData.getAccountsResponse());
        when(client.getBalance(any())).thenReturn(NorwegianFetcherTestData.getBalanceResponse());
        accountFetcher = new NorwegianTransactionalAccountFetcher(client);
    }

    @Test
    public void testAccount() {
        List<TransactionalAccount> accounts = new ArrayList<>(accountFetcher.fetchAccounts());
        assertThat(accounts).hasSize(1);
        assertThat(accounts.get(0).getExactBalance().getDoubleValue())
                .isEqualTo(Double.parseDouble(NorwegianFetcherTestData.BALANCE));
        assertThat(accounts.get(0).getIdentifiers().get(0).getIdentifier())
                .isEqualTo(NorwegianFetcherTestData.BBAN);
        assertThat(accounts.get(0).getApiIdentifier())
                .isEqualTo(NorwegianFetcherTestData.RESOURCE_ID);
    }
}
