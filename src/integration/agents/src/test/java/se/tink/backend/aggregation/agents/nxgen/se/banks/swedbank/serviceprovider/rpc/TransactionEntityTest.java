package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.util.Objects;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class TransactionEntityTest {

    @Test
    public void shouldMapTransaction() {
        TransactionEntity transactionEntity =
                SerializationUtils.deserializeFromString(
                        getTransaction("\"Betalning\""), TransactionEntity.class);
        Transaction result = transactionEntity.toTinkTransaction().orElse(null);

        Assert.assertEquals("Betalning", Objects.requireNonNull(result).getDescription());
        Assert.assertFalse(Objects.requireNonNull(result).isPending());
        Assert.assertEquals(
                "10.0", Objects.requireNonNull(result).getAmount().getExactValue().toString());
        Assert.assertEquals("SEK", Objects.requireNonNull(result).getAmount().getCurrencyCode());

        TransactionDetails detailsResult = transactionEntity.getTransactionDetails();

        Assert.assertEquals("this is my reference", detailsResult.transactionReference);
        Assert.assertEquals("this is what customer named it", detailsResult.ownReference);
    }

    @Test
    public void shouldMapPendingTransaction() {
        TransactionEntity transactionEntity =
                SerializationUtils.deserializeFromString(
                        getTransaction("\"ÖVF VIA INTERNET\""), TransactionEntity.class);
        Transaction result = transactionEntity.toTinkTransaction().orElse(null);

        Assert.assertEquals("ÖVF VIA INTERNET", Objects.requireNonNull(result).getDescription());
        Assert.assertTrue(Objects.requireNonNull(result).isPending());
        Assert.assertEquals(
                "10.0", Objects.requireNonNull(result).getAmount().getExactValue().toString());
        Assert.assertEquals("SEK", Objects.requireNonNull(result).getAmount().getCurrencyCode());

        TransactionDetails detailsResult = transactionEntity.getTransactionDetails();

        Assert.assertEquals("this is my reference", detailsResult.transactionReference);
        Assert.assertEquals("this is what customer named it", detailsResult.ownReference);
    }

    private String getTransaction(String description) {
        return "{"
                + "\"categoryId\": 0,"
                + "\"date\": \"2020-09-23\","
                + "\"amount\": \"10,00\","
                + "\"description\": "
                + description
                + ", \"currency\": \"SEK\","
                + "\"expenseControlIncluded\": \"UNAVAILABLE\","
                + "\"details\": {"
                + "\"transactionType\": \"Insättning\","
                + "\"reference\": \"this is my reference\","
                + "\"bankReference\": \"this is a bank reference\","
                + "\"message\": \"this is what customer named it\","
                + "\"transactionDate\": \"2020-09-23\","
                + "\"bookedDate\": \"2020-09-23\""
                + "},"
                + "\"accountingDate\": \"2020-09-23\","
                + "\"accountingBalance\": {"
                + "\"amount\": \"100 000,00\","
                + "\"currencyCode\": \"SEK\""
                + "},"
                + "\"bookedDate\": \"2020-09-23\""
                + "}";
    }
}
