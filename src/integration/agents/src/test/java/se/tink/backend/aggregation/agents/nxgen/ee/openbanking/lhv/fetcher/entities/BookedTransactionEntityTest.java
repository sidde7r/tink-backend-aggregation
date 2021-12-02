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
    public void shouldMapTransactionsCorrectly() {
        BookedTransactionEntity bookedTransactionEntity =
                SerializationUtils.deserializeFromString(
                        getTransactions(), BookedTransactionEntity.class);

        Transaction result = bookedTransactionEntity.toTinkTransaction();

        Assert.assertFalse(result.isPending());
        Assert.assertEquals("202106281378178164-302887546", result.getTransactionReference());
        Assert.assertEquals("Payment", result.getDescription());
        Assert.assertEquals(BigDecimal.valueOf(-75.55), result.getAmount().getExactValue());
        Assert.assertEquals("EUR", result.getAmount().getCurrencyCode());
        Assert.assertEquals(
                "202106281378178164-302887546",
                result.getExternalSystemIds()
                        .get(TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID));
        Assert.assertEquals("Tue Jan 28 11:00:00 UTC 1992", result.getDate().toString());
    }

    private String getTransactions() {
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
                + "\"remittanceInformationUnstructured\":\"Payment\"},"
                + "{\"entryReference\":\"202106301378679415-303289656\","
                + "\"bookingDate\":\"1992-01-30\","
                + "\"transactionAmount\":"
                + "{\"currency\":\"EUR\","
                + "\"amount\":\"660.00\"},"
                + "\"creditorName\":\"Jane Doe\","
                + "\"creditorAccount\":"
                + "{\"iban\":\"EE787700771005998888\"},"
                + "\"debtorName\":\"Esbjorn\","
                + "\"debtorAccount\":"
                + "{\"iban\":\"EE347700771005934444\"},"
                + "\"remittanceInformationUnstructured\":\"1\","
                + "\"remittanceInformationStructured\":\"1\"},"
                + "{\"entryReference\":\"202106301378706076-303310033\","
                + "\"bookingDate\":\"1992-01-30\","
                + "\"transactionAmount\":"
                + "{\"currency\":\"EUR\","
                + "\"amount\":\"500.00\"},"
                + "\"creditorName\":\"Jane Doe\","
                + "\"creditorAccount\":"
                + "{\"iban\":\"EE787700771005998888\"},"
                + "\"debtorName\":\"Esbjorn\","
                + "\"debtorAccount\":"
                + "{\"iban\":\"EE347700771005934444\"},"
                + "\"remittanceInformationUnstructured\":\"1\","
                + "\"remittanceInformationStructured\":\"1\"}"
                + "}";
    }
}
