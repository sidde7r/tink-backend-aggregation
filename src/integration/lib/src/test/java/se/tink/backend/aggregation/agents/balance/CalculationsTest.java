package se.tink.backend.aggregation.agents.balance;

import static org.assertj.core.api.Assertions.assertThat;
import static se.tink.backend.aggregation.agents.balance.Calculations.addBookedTransactionsWithBookingDateAfterBalanceSnapshot;
import static se.tink.backend.aggregation.agents.balance.Calculations.addPendingTransactionsWithBookingDateAfterBalanceSnapshot;
import static se.tink.backend.aggregation.agents.balance.Calculations.returnBalanceAmountAsIs;
import static se.tink.backend.aggregation.agents.balance.Calculations.subtractPendingTransactions;
import static se.tink.backend.aggregation.agents.balance.Calculations.subtractPendingTransactionsWithBookingDateAfterBalanceSnapshot;
import static se.tink.backend.aggregation.agents.balance.Calculations.sum;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class CalculationsTest {

    private static final ExactCurrencyAmount ONE_HUNDRED_EURO = ExactCurrencyAmount.inEUR(100.00);
    private static final String BOOKED_INCOMING_TRANSACTION_100_EUR_BEFORE_SNAPSHOT =
            "{\"amount\":100.00,\"pending\":false,\"transactionDates\":[{\"type\":\"BOOKING_DATE\",\"value\":{\"date\":\"2000-01-29\",\"instant\":\"2000-01-29T10:00:00.000Z\"}}]}";
    private static final String PENDING_INCOMING_TRANSACTION_10_EUR_BEFORE_SNAPSHOT =
            "{\"amount\":10.00,\"pending\":true,\"transactionDates\":[{\"type\":\"BOOKING_DATE\",\"value\":{\"date\":\"2000-01-30\",\"instant\":\"2000-01-30T10:00:00.000Z\"}}]}";
    private static final String PENDING_INCOMING_TRANSACTION_20_EUR_AFTER_SNAPSHOT =
            "{\"amount\":20.00,\"pending\":true,\"transactionDates\":[{\"type\":\"BOOKING_DATE\",\"value\":{\"date\":\"2000-01-31\",\"instant\":\"2000-01-31T10:00:00.000Z\"}}]}";
    private static final String BOOKED_INCOMING_TRANSACTION_50_EUR_AFTER_SNAPSHOT =
            "{\"amount\":50.00,\"pending\":false,\"transactionDates\":[{\"type\":\"BOOKING_DATE\",\"value\":{\"date\":\"2000-01-29\",\"instant\":\"2000-01-31T10:00:00.000Z\"}}]}";

    private static final Instant BALANCE_SNAPSHOT_INSTANT =
            LocalDate.of(2000, 1, 31).atStartOfDay().atOffset(ZoneOffset.UTC).toInstant();

    @Test
    public void shouldReturnBalanceAmountAsIs() {
        // given
        Pair<ExactCurrencyAmount, Instant> balanceWithSnapshotTime =
                Pair.of(ONE_HUNDRED_EURO, null);

        // when
        Pair<Optional<ExactCurrencyAmount>, CalculationSummary> result =
                returnBalanceAmountAsIs.evaluate(balanceWithSnapshotTime, Collections.emptyList());

        // then
        assertThat(result.getLeft()).isPresent().get().isEqualTo(ONE_HUNDRED_EURO);
    }

    @Test
    public void shouldSubtractPendingTransactions() {
        // given
        Pair<ExactCurrencyAmount, Instant> balanceWithSnapshotTime =
                Pair.of(ONE_HUNDRED_EURO, null);

        List<Transaction> transactions =
                Arrays.asList(
                        SerializationUtils.deserializeFromString(
                                BOOKED_INCOMING_TRANSACTION_100_EUR_BEFORE_SNAPSHOT,
                                Transaction.class),
                        SerializationUtils.deserializeFromString(
                                PENDING_INCOMING_TRANSACTION_10_EUR_BEFORE_SNAPSHOT,
                                Transaction.class),
                        SerializationUtils.deserializeFromString(
                                PENDING_INCOMING_TRANSACTION_20_EUR_AFTER_SNAPSHOT,
                                Transaction.class));

        // when
        Pair<Optional<ExactCurrencyAmount>, CalculationSummary> result =
                subtractPendingTransactions.evaluate(balanceWithSnapshotTime, transactions);

        // then
        assertThat(result.getLeft()).isPresent().get().isEqualTo(ExactCurrencyAmount.inEUR(70.00));
    }

    @Test
    public void shouldSubtractPendingTransactionsThatWillBeBookedAfterBalanceSnapshot() {
        // given
        Pair<ExactCurrencyAmount, Instant> balanceWithSnapshotTime =
                Pair.of(ONE_HUNDRED_EURO, BALANCE_SNAPSHOT_INSTANT);

        List<Transaction> transactions =
                Arrays.asList(
                        SerializationUtils.deserializeFromString(
                                BOOKED_INCOMING_TRANSACTION_100_EUR_BEFORE_SNAPSHOT,
                                Transaction.class),
                        SerializationUtils.deserializeFromString(
                                PENDING_INCOMING_TRANSACTION_10_EUR_BEFORE_SNAPSHOT,
                                Transaction.class),
                        SerializationUtils.deserializeFromString(
                                PENDING_INCOMING_TRANSACTION_20_EUR_AFTER_SNAPSHOT,
                                Transaction.class));

        // when
        Pair<Optional<ExactCurrencyAmount>, CalculationSummary> result =
                subtractPendingTransactionsWithBookingDateAfterBalanceSnapshot.evaluate(
                        balanceWithSnapshotTime, transactions);

        // then
        assertThat(result.getLeft()).isPresent().get().isEqualTo(ExactCurrencyAmount.inEUR(80.00));
    }

    @Test
    public void shouldAddPendingTransactionsThatWillBeBookedAfterBalanceSnapshot() {
        // given
        Pair<ExactCurrencyAmount, Instant> balanceWithSnapshotTime =
                Pair.of(ONE_HUNDRED_EURO, BALANCE_SNAPSHOT_INSTANT);

        List<Transaction> transactions =
                Arrays.asList(
                        SerializationUtils.deserializeFromString(
                                BOOKED_INCOMING_TRANSACTION_100_EUR_BEFORE_SNAPSHOT,
                                Transaction.class),
                        SerializationUtils.deserializeFromString(
                                PENDING_INCOMING_TRANSACTION_10_EUR_BEFORE_SNAPSHOT,
                                Transaction.class),
                        SerializationUtils.deserializeFromString(
                                PENDING_INCOMING_TRANSACTION_20_EUR_AFTER_SNAPSHOT,
                                Transaction.class));

        // when
        Pair<Optional<ExactCurrencyAmount>, CalculationSummary> result =
                addPendingTransactionsWithBookingDateAfterBalanceSnapshot.evaluate(
                        balanceWithSnapshotTime, transactions);

        // then
        assertThat(result.getLeft()).isPresent().get().isEqualTo(ExactCurrencyAmount.inEUR(120.00));
    }

    @Test
    public void shouldAddBookedTransactionsThatWillBeTrulyBookedAfterBalanceSnapshot() {
        // given
        Pair<ExactCurrencyAmount, Instant> balanceWithSnapshotTime =
                Pair.of(ONE_HUNDRED_EURO, BALANCE_SNAPSHOT_INSTANT);

        List<Transaction> transactions =
                Arrays.asList(
                        SerializationUtils.deserializeFromString(
                                BOOKED_INCOMING_TRANSACTION_100_EUR_BEFORE_SNAPSHOT,
                                Transaction.class),
                        SerializationUtils.deserializeFromString(
                                PENDING_INCOMING_TRANSACTION_10_EUR_BEFORE_SNAPSHOT,
                                Transaction.class),
                        SerializationUtils.deserializeFromString(
                                PENDING_INCOMING_TRANSACTION_20_EUR_AFTER_SNAPSHOT,
                                Transaction.class),
                        SerializationUtils.deserializeFromString(
                                BOOKED_INCOMING_TRANSACTION_50_EUR_AFTER_SNAPSHOT,
                                Transaction.class));

        // when
        Pair<Optional<ExactCurrencyAmount>, CalculationSummary> result =
                addBookedTransactionsWithBookingDateAfterBalanceSnapshot.evaluate(
                        balanceWithSnapshotTime, transactions);

        // then
        assertThat(result.getLeft()).isPresent().get().isEqualTo(ExactCurrencyAmount.inEUR(150.00));
    }

    @Test
    public void shouldReturnAsIsIfTransactionSumIsZero() {
        // given
        Pair<ExactCurrencyAmount, Instant> balanceWithSnapshotTime =
                Pair.of(ONE_HUNDRED_EURO, BALANCE_SNAPSHOT_INSTANT);

        List<Transaction> transactions =
                Collections.singletonList(
                        SerializationUtils.deserializeFromString(
                                BOOKED_INCOMING_TRANSACTION_100_EUR_BEFORE_SNAPSHOT,
                                Transaction.class));

        // when
        Pair<Optional<ExactCurrencyAmount>, CalculationSummary> result =
                addBookedTransactionsWithBookingDateAfterBalanceSnapshot.evaluate(
                        balanceWithSnapshotTime, transactions);

        // then
        assertThat(result.getLeft()).isPresent().get().isEqualTo(ExactCurrencyAmount.inEUR(100.00));
    }

    @Test
    public void shouldDefaultTransactionSumToZeroIfNoTransactions() {
        // when
        ExactCurrencyAmount result = sum(Collections.emptyList(), "EUR");

        // then
        assertThat(result).isEqualTo(ExactCurrencyAmount.zero("EUR"));
    }
}
