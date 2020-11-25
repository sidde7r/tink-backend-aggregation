package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.transactionalaccount.entities;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(JUnitParamsRunner.class)
public class TransactionEntityTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/no/banks/handelsbanken/resources";

    @Test
    @Parameters(method = "descriptionsBeforeAndAfter")
    public void testParseTransactionDescription(String rawDescription, String expectedResult) {
        assertThat(TransactionEntity.getTinkFormattedDescription(rawDescription))
                .isEqualTo(expectedResult);
    }

    @SuppressWarnings("unused")
    private Object[] descriptionsBeforeAndAfter() {
        return new Object[] {
            new Object[] {"01.01 MERCHANT NAME", "MERCHANT NAME"},
            new Object[] {"*1234 01.01 NOK 123.00 MERCHANT NAME Kurs: 1.0000", "MERCHANT NAME"},
            new Object[] {"NETTBANK OVERFØRSEL EGNE KONTI", "NETTBANK OVERFØRSEL EGNE KONTI"},
            new Object[] {
                "*1234 01.01 NOK 123.00 Vipps MERCHANT NAME Kurs: 1.0000", "Vipps MERCHANT NAME"
            },
            new Object[] {"*1234 01.01 NOK 1234.00 Vipps Kurs: 1.0000", "Vipps"}
        };
    }

    @Test
    public void shouldReturnParsedTransactionWithNullDescription() {
        // given
        TransactionEntity transactionEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transactionWithNullDescription.json").toFile(),
                        TransactionEntity.class);

        // when
        Transaction transaction = transactionEntity.toTinkTransaction();

        // then
        assertThat(transaction.getDescription()).isNull();
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getDate()).isEqualToIgnoringHours("2019-12-23");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.of(-450, "NOK"));
    }

    @Test
    public void shouldReturnParsedTransactionWithDescription() {
        // given
        TransactionEntity transactionEntity =
                SerializationUtils.deserializeFromString(
                        Paths.get(TEST_DATA_PATH, "transaction.json").toFile(),
                        TransactionEntity.class);

        // when
        Transaction transaction = transactionEntity.toTinkTransaction();

        // then
        assertThat(transaction.getDescription())
                .isEqualTo("Nettgiro til: Asdzxcv Betalt: 16.12.19");
        assertThat(transaction.isPending()).isFalse();
        assertThat(transaction.getDate()).isEqualToIgnoringHours("2019-12-16");
        assertThat(transaction.getExactAmount()).isEqualTo(ExactCurrencyAmount.of(-20_000, "NOK"));
    }
}
