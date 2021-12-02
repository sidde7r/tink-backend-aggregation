package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.rpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sdcse.parser.SdcSeTransactionParser;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class SearchTransactionsResponseTest {
    @Test
    public void getTinkTransactions() {
        SearchTransactionsResponse response = SearchTransactionsResponseTestData.getTestData();

        SdcSeTransactionParser transactionParser = new SdcSeTransactionParser();
        Collection<Transaction> transactions = response.getTinkTransactions(transactionParser);

        assertThat(transactions).isNotEmpty();
        for (Transaction transaction : transactions) {
            assertThat(transaction.getDescription()).isNotNull();
            assertThat(transaction.getDate()).isNotNull();
            assertThat(transaction.getExactAmount().getDoubleValue()).isNotEqualTo(0);
        }
    }

    @Test
    public void getTinkCreditCardTransactions() {
        SearchTransactionsResponse response =
                SearchCreditCardTransactionsResponseTestData.getTestData();

        CreditCardAccount creditCardAccount =
                CreditCardAccount.builder(
                                "uniqueIdentifier",
                                ExactCurrencyAmount.inSEK(9500.0),
                                ExactCurrencyAmount.inSEK(10500.0))
                        .setAccountNumber("0123456789")
                        .setName("Credit-card")
                        .build();

        SdcSeTransactionParser transactionParser = new SdcSeTransactionParser();
        Collection<CreditCardTransaction> transactions =
                response.getTinkCreditCardTransactions(creditCardAccount, transactionParser);

        assertThat(transactions).isNotEmpty();
        assertThat(transactions.stream().filter(Transaction::isPending).count()).isEqualTo(1);
        assertThat(transactions.stream().filter(t -> !t.isPending()).count()).isEqualTo(6);
        for (CreditCardTransaction transaction : transactions) {
            assertThat(transaction.getDescription()).isNotNull();
            assertThat(transaction.getDate()).isNotNull();
            assertThat(transaction.getCreditCardAccountNumber().isPresent()).isTrue();
            assertThat(transaction.getExactAmount().getDoubleValue()).isNotEqualTo(0);
        }
    }

    @Test
    public void getTinkTransactionsWithReservations() {
        SearchTransactionsResponse response =
                SearchTransactionsResponseTestData.getTestDataWithReservations();

        SdcSeTransactionParser transactionParser = new SdcSeTransactionParser();
        Collection<Transaction> transactions = response.getTinkTransactions(transactionParser);

        assertThat(transactions).isNotEmpty();
        assertEquals(1, transactions.stream().filter(Transaction::isPending).count());
        for (Transaction transaction : transactions) {
            assertThat(transaction.getDescription()).isNotNull();
            assertThat(transaction.getDate()).isNotNull();
            assertThat(transaction.getExactAmount().getDoubleValue()).isNotEqualTo(0);
        }
    }

    @Test
    public void getTinkTransactionsEmptyResponse() {
        SearchTransactionsResponse response = SearchTransactionsResponseTestData.getTestEmptyData();

        SdcSeTransactionParser transactionParser = new SdcSeTransactionParser();
        Collection<Transaction> transactions = response.getTinkTransactions(transactionParser);

        assertThat(transactions).isEmpty();
    }
}
