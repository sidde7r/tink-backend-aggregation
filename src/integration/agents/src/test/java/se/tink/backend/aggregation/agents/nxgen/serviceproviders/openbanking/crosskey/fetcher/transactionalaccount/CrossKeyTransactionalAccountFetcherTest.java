package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.transactionalaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
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
import se.tink.libraries.account.enums.AccountFlag;
import se.tink.libraries.account.identifiers.BbanIdentifier;
import se.tink.libraries.account.identifiers.IbanIdentifier;

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

        when(apiClient.fetchAccountBalances("SE4550000000058398257466"))
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
        assertEquals("SE4550000000058398257466", transactionalAccount.getApiIdentifier());
        assertEquals("EUR", transactionalAccount.getExactBalance().getCurrencyCode());
        assertTrue(
                transactionalAccount
                        .getIdentifiersAsList()
                        .contains(new IbanIdentifier("SE4550000000058398257466")));
        assertTrue(
                transactionalAccount
                        .getIdentifiersAsList()
                        .contains(new BbanIdentifier("50000000058398257466")));
        assertEquals(50.0, transactionalAccount.getExactBalance().getDoubleValue(), 0.001);
        assertEquals(
                AccountFlag.PSD2_PAYMENT_ACCOUNT, transactionalAccount.getAccountFlags().get(0));
    }
}
