package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.client.NorwegianApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.fetcher.account.data.NorwegianFetcherTestData;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class NorwegianAccountFetcherTest {

    private NorwegianAccountFetcher accountFetcher;

    @Before
    public void init() {
        NorwegianApiClient client = mock(NorwegianApiClient.class);
        when(client.fetchAccounts()).thenReturn(NorwegianFetcherTestData.getAccountsResponse());
        when(client.getBalance(NorwegianFetcherTestData.ACCOUNT_1_RESOURCE_ID))
                .thenReturn(NorwegianFetcherTestData.getBalances1Response());
        accountFetcher = new NorwegianAccountFetcher(client);
    }

    @Test
    public void shouldResultInExactlyOneProperlyMappedSavingsAccount() {

        // when
        List<TransactionalAccount> accounts = new ArrayList<>(accountFetcher.fetchAccounts());

        // then
        assertThat(accounts).hasSize(1);
        TransactionalAccount account = accounts.get(0);
        assertThat(account.getType()).isEqualTo(AccountTypes.SAVINGS);
        assertThat(account.getExactBalance().getDoubleValue())
                .isEqualTo(Double.parseDouble(NorwegianFetcherTestData.BALANCE_1));
        assertThat(account.isUniqueIdentifierEqual(NorwegianFetcherTestData.ACCOUNT_1_RESOURCE_ID))
                .isTrue();
        assertThat(account.getAccountNumber()).isEqualTo(NorwegianFetcherTestData.ACCOUNT_1_BBAN);
        assertThat(account.getName()).isEqualTo(NorwegianFetcherTestData.ACCOUNT_1_NAME);
        assertThat(account.getIdentifiers().get(0).getIdentifier())
                .isEqualTo(NorwegianFetcherTestData.ACCOUNT_1_BBAN);
        assertThat(account.getApiIdentifier())
                .isEqualTo(NorwegianFetcherTestData.ACCOUNT_1_RESOURCE_ID);
    }
}
