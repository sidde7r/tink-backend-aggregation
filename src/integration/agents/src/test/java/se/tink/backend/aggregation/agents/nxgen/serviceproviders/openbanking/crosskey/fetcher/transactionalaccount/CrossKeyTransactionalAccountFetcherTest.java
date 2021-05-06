package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrossKeyTestUtils.loadResourceFileContent;

import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountBalancesResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.rpc.CrosskeyAccountsResponse;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@RunWith(MockitoJUnitRunner.class)
public class CrossKeyTransactionalAccountFetcherTest {

    @Mock private CrosskeyBaseApiClient apiClient;

    private CrossKeyTransactionalAccountFetcher accountFetcher;

    @Before
    public void init() {
        accountFetcher = new CrossKeyTransactionalAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchCheckingAccounts() {
        // given
        when(apiClient.fetchAccounts())
                .thenReturn(
                        loadResourceFileContent(
                                "checkingAccountResponse.json", CrosskeyAccountsResponse.class));

        when(apiClient.fetchAccountBalances("AccountId1"))
                .thenReturn(
                        loadResourceFileContent(
                                "accountTransactionResponse.json",
                                CrosskeyAccountBalancesResponse.class));

        // when
        List<TransactionalAccount> accounts = new ArrayList(accountFetcher.fetchAccounts());
        // then
        assertEquals(1, accounts.size());

        TransactionalAccount transactionalAccount = accounts.get(0);
        assertEquals(AccountTypes.CHECKING, transactionalAccount.getType());
        assertEquals("AccountId1", transactionalAccount.getApiIdentifier());
        assertEquals("EUR", transactionalAccount.getExactBalance().getCurrencyCode());
        assertEquals(50.0, transactionalAccount.getExactBalance().getDoubleValue(), 0.001);
    }
}
