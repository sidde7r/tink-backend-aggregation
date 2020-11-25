package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.fetcher.creditcard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.OpBankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class OpBankCreditCardAccountFetcherTest {;

    OpBankCreditCardAccountFetcher objectUnderTest;
    private OpBankApiClient apiClient;

    @Before
    public void setup() {
        apiClient = mock(OpBankApiClient.class);
        objectUnderTest = new OpBankCreditCardAccountFetcher(apiClient);
    }

    @Test
    public void shouldFetchCreditCardAccounts() {
        // given
        givenApiClientWillReturnCreditCards();
        // when
        Collection<CreditCardAccount> creditCardAccounts = objectUnderTest.fetchAccounts();
        // then
        assertThat(creditCardAccounts).hasSize(2);
        CreditCardAccount card1 =
                creditCardAccounts.stream()
                        .filter(
                                account ->
                                        account.getCardModule()
                                                .getCardNumber()
                                                .equals("1234 56** **** 7890"))
                        .findAny()
                        .orElseThrow(IllegalArgumentException::new);
        CreditCardAccount card2 =
                creditCardAccounts.stream()
                        .filter(
                                account ->
                                        account.getCardModule()
                                                .getCardNumber()
                                                .equals("0987 65** **** 4321"))
                        .findAny()
                        .orElseThrow(IllegalArgumentException::new);
        assertThat(card1.getName()).isEqualTo("testProductName1");
        assertThat(card1.getAccountNumber()).isEqualTo("1234 56** **** 7890");
        assertThat(card1.getIdModule().getUniqueId()).isEqualTo("testCardId1");
        assertThat(card2.getName()).isEqualTo("testProductName2");
        assertThat(card2.getAccountNumber()).isEqualTo("0987 65** **** 4321");
        assertThat(card2.getIdModule().getUniqueId()).isEqualTo("testCardId2");
    }

    @Test
    public void shouldFetchCredittCardTransactions() {
        // given
        CreditCardAccount creditCardAccount = givenSomeCreditCardAccount();
        givenApiClientWillReturn2PagesOfTransactions();
        // when
        TransactionKeyPaginatorResponse<URL> transactionsPage1 =
                objectUnderTest.getTransactionsFor(creditCardAccount, null);
        // then
        assertThat(transactionsPage1.getTinkTransactions()).hasSize(1);
        assertThat(transactionsPage1.nextKey())
                .isEqualTo(new URL("https://mtls.apis.op.fi/nextKey"));
        Transaction transaction1 =
                transactionsPage1.getTinkTransactions().stream()
                        .findAny()
                        .orElseThrow(IllegalArgumentException::new);
        assertThat(transaction1.getExactAmount().getExactValue()).isEqualByComparingTo("3.50");
        assertThat(transaction1.getDescription()).isEqualTo("testDescription");
        assertThat(transaction1.getDate()).isEqualToIgnoringHours("2020-11-09");
        // and when
        TransactionKeyPaginatorResponse<URL> transactionsPage2 =
                objectUnderTest.getTransactionsFor(creditCardAccount, transactionsPage1.nextKey());
        // then
        assertThat(transactionsPage2.nextKey()).isNull();
        assertThat(transactionsPage2.getTinkTransactions()).hasSize(1);
    }

    private void givenApiClientWillReturnCreditCards() {
        when(apiClient.getCreditCards())
                .thenReturn(OpBankCreditCardAccountFetcherTestFixtures.creditCardsResponse());
    }

    private void givenApiClientWillReturn2PagesOfTransactions() {
        when(apiClient.getCreditCardTransactions(
                        eq(
                                new URL(
                                        "https://mtls.apis.op.fi/accounts-psd2/v1/cards/testCardId/transactions"))))
                .thenReturn(
                        OpBankCreditCardAccountFetcherTestFixtures.creditCardTransactionsPage1());
        when(apiClient.getCreditCardTransactions(eq(new URL("https://mtls.apis.op.fi/nextKey"))))
                .thenReturn(
                        OpBankCreditCardAccountFetcherTestFixtures.creditCardTransactionsPage2());
    }

    private CreditCardAccount givenSomeCreditCardAccount() {
        CreditCardAccount result = mock(CreditCardAccount.class);
        when(result.getFromTemporaryStorage(eq("cardId"))).thenReturn("testCardId");
        return result;
    }
}
