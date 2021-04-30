package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount;

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
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

@RunWith(MockitoJUnitRunner.class)
public class CrossKeyCreditCardAccountFetcherTest {

    @Mock CrosskeyBaseApiClient apiClient;

    private CrossKeyCreditCardAccountFetcher creditCardAccountFetcher;

    @Before
    public void init() {
        creditCardAccountFetcher = new CrossKeyCreditCardAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchCardAccounts() {
        // given
        when(apiClient.fetchAccounts())
                .thenReturn(
                        loadResourceFileContent(
                                "cardAccountResponse.json", CrosskeyAccountsResponse.class));

        when(apiClient.fetchAccountBalances("1111Card"))
                .thenReturn(
                        loadResourceFileContent(
                                "accountTransactionResponse.json",
                                CrosskeyAccountBalancesResponse.class));

        // when
        List<CreditCardAccount> accounts = new ArrayList(creditCardAccountFetcher.fetchAccounts());

        // then
        assertEquals(1, accounts.size());
        CreditCardAccount cardAccount = accounts.get(0);
        assertEquals(AccountTypes.CREDIT_CARD, cardAccount.getType());
        assertEquals("1111Card", cardAccount.getApiIdentifier());
        assertEquals("EUR", cardAccount.getExactBalance().getCurrencyCode());
        assertEquals(50.0, cardAccount.getExactBalance().getDoubleValue(), 0.001);
    }
}
