package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.entities;

import java.math.BigDecimal;
import org.junit.Assert;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.fetcher.transactional.entities.BookedTransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BookedTransactionEntityTest {

    @Test
    public void shouldMapDebitTransactionsCorrectly() {
        BookedTransactionEntity bookedTransactionEntity =
                SerializationUtils.deserializeFromString(
                        getDebitTransaction(), BookedTransactionEntity.class);

        Transaction result = bookedTransactionEntity.toTinkTransaction();

        Assert.assertFalse(result.isPending());
        Assert.assertEquals("202106281378178164-302887546", result.getTransactionReference());
        Assert.assertEquals("Jane Doe", result.getDescription());
        Assert.assertEquals(BigDecimal.valueOf(-75.55), result.getAmount().getExactValue());
        Assert.assertEquals("EUR", result.getAmount().getCurrencyCode());
        Assert.assertEquals(
                "202106281378178164-302887546",
                result.getExternalSystemIds()
                        .get(TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID));
        Assert.assertEquals("Tue Jan 28 11:00:00 UTC 1992", result.getDate().toString());
    }

    @Test
    public void shouldMapCreditTransactionsCorrectly() {
        BookedTransactionEntity bookedTransactionEntity =
                SerializationUtils.deserializeFromString(
                        getCreditTransaction(), BookedTransactionEntity.class);

        Transaction result = bookedTransactionEntity.toTinkTransaction();

        Assert.assertFalse(result.isPending());
        Assert.assertEquals("202106281378178164-302887546", result.getTransactionReference());
        Assert.assertEquals("Esbjorn", result.getDescription());
        Assert.assertEquals(BigDecimal.valueOf(5005.55), result.getAmount().getExactValue());
        Assert.assertEquals("EUR", result.getAmount().getCurrencyCode());
        Assert.assertEquals(
                "202106281378178164-302887546",
                result.getExternalSystemIds()
                        .get(TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID));
        Assert.assertEquals("Tue Jan 28 11:00:00 UTC 1992", result.getDate().toString());
    }

    @Test
    public void shouldMapCreditTransactionsCorrectlyWhenDebtorNameIsNull() {
        BookedTransactionEntity bookedTransactionEntity =
                SerializationUtils.deserializeFromString(
                        getCreditorNameNullTransaction(), BookedTransactionEntity.class);

        Transaction result = bookedTransactionEntity.toTinkTransaction();

        Assert.assertFalse(result.isPending());
        Assert.assertEquals("202106281378178164-302887546", result.getTransactionReference());
        Assert.assertEquals("Swish", result.getDescription());
        Assert.assertEquals(BigDecimal.valueOf(5005.55), result.getAmount().getExactValue());
        Assert.assertEquals("EUR", result.getAmount().getCurrencyCode());
        Assert.assertEquals(
                "202106281378178164-302887546",
                result.getExternalSystemIds()
                        .get(TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID));
        Assert.assertEquals("Tue Jan 28 11:00:00 UTC 1992", result.getDate().toString());
    }

    private String getDebitTransaction() {
        return "{\"entryReference\":\"202106281378178164-302887546\","
                + "\"bookingDate\":\"1992-01-28\","
                + "\"transactionAmount\":"
                + "{\"currency\":\"EUR\","
                + "\"amount\":\"-75.55\"},"
                + "\"creditorName\":\"Jane Doe\","
                + "\"creditorAccount\":"
                + "{\"iban\":\"EE347700771005934444\"},"
                + "\"debtorName\":\"Esbjorn\","
                + "\"debtorAccount\":"
                + "{\"iban\":\"EE787700771005998888\"},"
                + "\"remittanceInformationUnstructured\":\"Payment\"}"
                + "}";
    }

    private String getCreditTransaction() {
        return "{\"entryReference\":\"202106281378178164-302887546\","
                + "\"bookingDate\":\"1992-01-28\","
                + "\"transactionAmount\":"
                + "{\"currency\":\"EUR\","
                + "\"amount\":\"5005.55\"},"
                + "\"creditorName\":\"Jane Doe\","
                + "\"creditorAccount\":"
                + "{\"iban\":\"EE347700771005934444\"},"
                + "\"debtorName\":\"Esbjorn\","
                + "\"debtorAccount\":"
                + "{\"iban\":\"EE787700771005998888\"},"
                + "\"remittanceInformationUnstructured\":\"Swish\"}"
                + "}";
    }

    private String getCreditorNameNullTransaction() {
        return "{\"entryReference\":\"202106281378178164-302887546\","
                + "\"bookingDate\":\"1992-01-28\","
                + "\"transactionAmount\":"
                + "{\"currency\":\"EUR\","
                + "\"amount\":\"5005.55\"},"
                + "\"creditorAccount\":"
                + "{\"iban\":\"EE347700771005934444\"},"
                + "\"debtorAccount\":"
                + "{\"iban\":\"EE787700771005998888\"},"
                + "\"remittanceInformationUnstructured\":\"Swish\"}"
                + "}";
    }
}
