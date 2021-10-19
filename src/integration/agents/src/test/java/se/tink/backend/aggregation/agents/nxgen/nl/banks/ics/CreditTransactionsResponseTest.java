package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.rpc.CreditTransactionsResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class CreditTransactionsResponseTest {

    private CreditTransactionsResponse creditTransactionsResponse;

    @Test
    public void shouldReturnEmptyCollectionOfTransactionsIfDataIsNull() {
        // given
        creditTransactionsResponse = TestHelper.getCreditTransactionResponseWithEmptyData();

        // then
        assertEquals(Collections.emptyList(), creditTransactionsResponse.getTinkTransactions());
    }

    @Test
    public void shouldReturnCollectionOfTransactionsFromGivenData() {
        // given
        creditTransactionsResponse = TestHelper.getCreditTransactionResponse();

        // then
        assertThat(creditTransactionsResponse.getTinkTransactions())
                .usingRecursiveComparison()
                .isEqualTo(getTransactions());
    }

    private Collection<Transaction> getTransactions() {
        Collection<Transaction> transactionCollection = new ArrayList<>();
        Transaction transaction1 =
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(Double.parseDouble("204.64"), "EUR"))
                        .setDescription("IDEAL BETALING, DANK U")
                        .setDate(LocalDate.of(1992, 1, 31))
                        .setPayload(TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL, "N/A")
                        .setPayload(TransactionPayloadTypes.MESSAGE, "N/A")
                        .build();

        Transaction transaction2 =
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(Double.parseDouble("-1.36"), "EUR"))
                        .setDescription("Januszpol retail stor")
                        .setDate(LocalDate.of(1992, 1, 21))
                        .setPayload(
                                TransactionPayloadTypes.TRANSFER_ACCOUNT_NAME_EXTERNAL,
                                "Januszpol retail stor")
                        .setPayload(TransactionPayloadTypes.MESSAGE, "Miscellaneous Retail Stor")
                        .build();

        transactionCollection.add(transaction1);
        transactionCollection.add(transaction2);
        return transactionCollection;
    }
}
