package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities;

import static org.junit.Assert.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.DateUtils;

public class TransactionEntityTest {

    private static final String TRANSACTION_TEST_DATA =
            "{"
                    + "\"id\":\"1234567\","
                    + "\"accountNumber\":\"123456789\","
                    + "\"prettyDescription\":\"\","
                    + "\"postingDate\":\"2018-01-26T00:00:00\","
                    + "\"accountingDate\":\"2018-01-26T00:00:00\","
                    + "\"valueDate\":\"2018-01-27T00:00:00\","
                    + "\"amount\":-30.0,"
                    + "\"archiveRef\":\"1234567\","
                    + "\"serialNo\":1,"
                    + "\"numRef\":0,"
                    + "\"alfaRef\":\"\","
                    + "\"txCode\":123,"
                    + "\"descriptionShort\":\"VARER\","
                    + "\"description\":\"26.01 SPOTIFY ADDRESS CITY\","
                    + "\"balanceAfterTransaction\":5000.0,"
                    + "\"draweeAccount\":\"00000000000\","
                    + "\"orgUnit\":\"99999\","
                    + "\"functionCode\":2000760,"
                    + "\"movementDetailIndex\":1"
                    + "}";

    @Test
    public void testTransactionParsing() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TransactionEntity transactionEntity =
                mapper.readValue(TRANSACTION_TEST_DATA, TransactionEntity.class);

        Transaction transaction = transactionEntity.toTinkTransaction();

        assertEquals(transaction.getAmount(), Amount.inNOK(-30.0));
        assertEquals("SPOTIFY ADDRESS CITY", transaction.getDescription());
        assertEquals(
                DateUtils.flattenTime(DateUtils.parseDate("2018-01-26")), transaction.getDate());
    }
}
