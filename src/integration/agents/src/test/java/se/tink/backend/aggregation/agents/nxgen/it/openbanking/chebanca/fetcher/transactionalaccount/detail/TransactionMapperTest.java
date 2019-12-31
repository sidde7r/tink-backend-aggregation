package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.detail;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.RequiredDataMissingException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.data.TransactionTestData;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionMapperTest {
    private final String AMOUNT = "50.000";
    private final String AMOUNT_NONE = null;
    private final String CURRENCY = "EUR";
    private final String DESCRIPTION = "ADDEBITO BONIFICO";
    private final String ACCOUNTING_DATE = "20181016";
    private final String LIQUIDATION_DATE = "20181015";
    private final String DATE_NONE = null;

    @Test
    public void shouldMapPendingTransactionCorrectly() {
        // given
        String expectedAccountingDate = DATE_NONE;
        String expectedLiquidationDate = LIQUIDATION_DATE;
        TransactionEntity transactionEntity =
                TransactionTestData.getTransactionEntity(
                        expectedAccountingDate, expectedLiquidationDate);

        // when
        Transaction transaction = TransactionMapper.toTinkTransaction(transactionEntity, true);

        // then
        assertEquals(DESCRIPTION, transaction.getDescription());
        assertEquals(ExactCurrencyAmount.of(AMOUNT, CURRENCY), transaction.getExactAmount());
        assertEquals(expectedLiquidationDate, dateToString(transaction.getDate()));
        assertTrue(transaction.isPending());
    }

    @Test
    public void shouldMapAccountingTransactionCorrectly() {
        // given
        String expectedAccountingDate = ACCOUNTING_DATE;
        String expectedLiquidationDate = LIQUIDATION_DATE;
        TransactionEntity transactionEntity =
                TransactionTestData.getTransactionEntity(
                        expectedAccountingDate, expectedLiquidationDate);

        // when
        Transaction transaction = TransactionMapper.toTinkTransaction(transactionEntity, false);

        // then
        assertEquals(DESCRIPTION, transaction.getDescription());
        assertEquals(ExactCurrencyAmount.of(AMOUNT, CURRENCY), transaction.getExactAmount());
        assertEquals(expectedAccountingDate, dateToString(transaction.getDate()));
        assertFalse(transaction.isPending());
    }

    @Test
    public void shouldThrowIfPendingTransactionIsMissingLiquidationDate() {
        // given
        String accountingDate = ACCOUNTING_DATE;
        String liquidationDate = DATE_NONE;
        TransactionEntity transactionEntity =
                TransactionTestData.getTransactionEntity(accountingDate, liquidationDate);

        // when
        Throwable thrown =
                catchThrowable(() -> TransactionMapper.toTinkTransaction(transactionEntity, true));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(RequiredDataMissingException.class)
                .hasMessage("Could not parse the given transaction date");
    }

    @Test
    public void shouldThrowIfAccountingTransactionIsMissingLiquidationDate() {
        // given
        String accountingDate = ACCOUNTING_DATE;
        String liquidationDate = DATE_NONE;
        TransactionEntity transactionEntity =
                TransactionTestData.getTransactionEntity(accountingDate, liquidationDate);

        // when
        Throwable thrown =
                catchThrowable(() -> TransactionMapper.toTinkTransaction(transactionEntity, false));

        // then
        assertNull(thrown);
    }

    @Test
    public void shouldThrowIfAmountIsMissing() {
        // given
        TransactionEntity transactionEntity =
                TransactionTestData.getTransactionEntity(
                        ACCOUNTING_DATE, LIQUIDATION_DATE, AMOUNT_NONE);

        // when
        Throwable thrown =
                catchThrowable(() -> TransactionMapper.toTinkTransaction(transactionEntity, false));

        // then
        Assertions.assertThat(thrown)
                .isInstanceOf(RequiredDataMissingException.class)
                .hasMessage("No transaction's amount data present");
    }

    private String dateToString(Date date) {
        return new SimpleDateFormat("yyyyMMdd").format(date);
    }
}
