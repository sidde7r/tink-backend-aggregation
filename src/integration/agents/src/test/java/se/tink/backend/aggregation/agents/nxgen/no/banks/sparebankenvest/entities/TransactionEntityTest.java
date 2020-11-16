package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.entities;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Paths;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.DateUtils;

public class TransactionEntityTest {

    private static final String TRANSACTION_TEST_DATA =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/sparebankenvest/resources/transactionTestData.json";

    @Test
    public void testTransactionParsing() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        TransactionEntity transactionEntity =
                mapper.readValue(
                        Paths.get(TRANSACTION_TEST_DATA).toFile(), TransactionEntity.class);

        Transaction transaction = transactionEntity.toTinkTransaction();

        assertThat(transaction.getExactAmount())
                .isEqualTo(new ExactCurrencyAmount(BigDecimal.valueOf(-30.0), "NOK"));
        assertThat("SPOTIFY ADDRESS CITY").isEqualTo(transaction.getDescription());
        assertThat(DateUtils.flattenTime(DateUtils.parseDate("2018-01-26")))
                .isEqualTo(transaction.getDate());
    }
}
