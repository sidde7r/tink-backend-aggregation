package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
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

        assertThat(transaction.getExactAmount())
                .isEqualTo(new ExactCurrencyAmount(new BigDecimal(-30.0), "NOK"));
        assertThat("SPOTIFY ADDRESS CITY").isEqualTo(transaction.getDescription());
        assertThat(DateUtils.flattenTime(DateUtils.parseDate("2018-01-26")))
                .isEqualTo(transaction.getDate());
    }
}
