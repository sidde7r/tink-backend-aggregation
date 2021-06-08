package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.creditcard.LclCreditCardFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class LclCreditCardFetcherTest {

    private LclApiClient lclApiClient;
    private LclCreditCardFetcher objectUnderTest;

    @Before
    public void before() {
        lclApiClient = mock(LclApiClient.class);
        objectUnderTest = new LclCreditCardFetcher(lclApiClient);
    }

    @Test
    public void shouldFetchCreditCardsAccounts() {
        // given
        when(lclApiClient.getAccountsResponse())
                .thenReturn(LclCreditCardFetcherTestData.ACCOUNTS_CARDS_RESPONSE);

        // when
        Collection<CreditCardAccount> response = objectUnderTest.fetchAccounts();

        // then
        CreditCardAccount creditCardAccount = response.iterator().next();
        assertThat(response).isNotNull();
        assertThat(response.size()).isEqualTo(1);
        assertThat(creditCardAccount.getExactAvailableCredit())
                .isEqualTo(ExactCurrencyAmount.of(0, "EUR"));
        assertThat(creditCardAccount.getExactBalance())
                .isEqualTo(ExactCurrencyAmount.of(100, "EUR"));
        assertThat(creditCardAccount.getName()).isEqualTo("CREDITE CARTE EL FRANCUSO");
        assertThat(creditCardAccount.getCardModule().getCardNumber()).isEqualTo("312321412345");
        assertThat(creditCardAccount.getAccountNumber()).isEqualTo("31232141");
    }

    @Test
    public void shouldFetchCreditCardsTransactions() {
        // given
        CreditCardAccount account = mock(CreditCardAccount.class);
        Mockito.when(account.getApiIdentifier()).thenReturn("resourceId");
        when(lclApiClient.getTransactionsResponse("resourceId", 1))
                .thenReturn(LclCreditCardFetcherTestData.CREDIT_CARD_TRANSACTIONS);

        // when
        PaginatorResponse response = objectUnderTest.getTransactionsFor(account, 1);

        // then
        List<Transaction> transactions = new LinkedList<>(response.getTinkTransactions());
        assertThat(transactions).hasSize(2);
    }
}
