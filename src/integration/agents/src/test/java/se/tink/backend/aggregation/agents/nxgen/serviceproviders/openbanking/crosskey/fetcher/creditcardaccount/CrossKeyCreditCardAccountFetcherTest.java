package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.creditcardaccount;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
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
import se.tink.libraries.account.enums.AccountIdentifierType;

@RunWith(MockitoJUnitRunner.class)
public class CrossKeyCreditCardAccountFetcherTest {
    @Mock CrosskeyBaseApiClient apiClient;
    CrosskeyBaseApiClient spyClient;
    private CrossKeyCreditCardAccountFetcher creditCardAccountFetcher;

    @Before
    public void init() {
        spyClient = spy(apiClient);
        creditCardAccountFetcher = new CrossKeyCreditCardAccountFetcher(spyClient);
    }

    @Test
    public void shouldFetchCardAccounts() {
        // given
        doReturn(
                        loadResourceFileContent(
                                "cardAccountResponse.json", CrosskeyAccountsResponse.class))
                .when(spyClient)
                .fetchAccounts();

        doReturn(
                        loadResourceFileContent(
                                "accountTransactionResponse.json",
                                CrosskeyAccountBalancesResponse.class))
                .when(spyClient)
                .fetchAccountBalances("SE4550000000058398257466");

        // when
        List<CreditCardAccount> accounts = new ArrayList(creditCardAccountFetcher.fetchAccounts());

        // then
        assertEquals(1, accounts.size());
        CreditCardAccount cardAccount = accounts.get(0);
        assertEquals(AccountTypes.CREDIT_CARD, cardAccount.getType());
        assertEquals("SE4550000000058398257466", cardAccount.getApiIdentifier());
        assertEquals(
                "SE4550000000058398257466",
                cardAccount.getIdentifiers().stream().findFirst().get().getIdentifier());
        assertTrue(
                cardAccount.getIdentifiers().stream()
                        .findFirst()
                        .get()
                        .is(AccountIdentifierType.PAYMENT_CARD_NUMBER));
        assertEquals("EUR", cardAccount.getExactBalance().getCurrencyCode());
        assertEquals(50.0, cardAccount.getExactBalance().getDoubleValue(), 0.001);
    }
}
