package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.component.transactional.detail;

import static org.junit.Assert.assertEquals;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.detail.TransactionMapper;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@RunWith(Parameterized.class)
public class TransactionMapperTest {
    private final String AMOUNT = "50.000";
    private final String CURRENCY = "EUR";
    private final String DESCRIPTION = "ADDEBITO BONIFICO";
    private final String DATE = "20181016";
    private final String TIME = "T00:00:00";

    private boolean isPendingTransaction;
    private boolean expectedIsPendingTransaction;

    public TransactionMapperTest(
            boolean isPendingTransaction, boolean expectedIsPendingTransaction) {
        this.isPendingTransaction = isPendingTransaction;
        this.expectedIsPendingTransaction = expectedIsPendingTransaction;
    }

    @Test
    public void testTransactionMappedCorrectly() {
        Transaction transaction =
                TransactionMapper.toTinkTransaction(getTransactionEntity(), isPendingTransaction);
        assertEquals(DESCRIPTION, transaction.getDescription());
        assertEquals(ExactCurrencyAmount.of(AMOUNT, CURRENCY), transaction.getExactAmount());
        assertEquals(DATE, dateToString(transaction.getDate()));
        assertEquals(expectedIsPendingTransaction, transaction.isPending());
    }

    private String dateToString(Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }

    private TransactionEntity getTransactionEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "\"shortDescription\": \""
                        + DESCRIPTION
                        + "\",\n"
                        + "\"extendedDescription\": \"ADDEBITO BONIFICO BLA BLA\",\n"
                        + "\"amountTransaction\": {\n"
                        + "\"amount\": \""
                        + AMOUNT
                        + "\",\n"
                        + "\"currency\": \""
                        + CURRENCY
                        + "\"\n"
                        + "},\n"
                        + "\"dateAccountingCurrency\": \""
                        + DATE
                        + TIME
                        + "\",\n"
                        + "\"dateLiquidationValue\": \"\",\n"
                        + "\"codeDescription\": \"GRAC01\"\n"
                        + "}",
                TransactionEntity.class);
    }

    @Parameterized.Parameters(name = "{index}: Test with isPendingTransaction={0}, result: {1}")
    public static Iterable<Object[]> data() {
        return Arrays.asList(
                new Object[][] {
                    {true, true},
                    {false, false}
                });
    }
}
