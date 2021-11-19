package se.tink.backend.aggregation.agents.balance;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

@SuppressWarnings("unused")
@RunWith(JUnitParamsRunner.class)
public class TransactionBookingDateComparatorTest {

    private static final String TRX_WITHOUT_DATES = "{\"transactionDates\":[]}";
    private static final String TRX_WITH_EMPTY_BOOKING_DATE_PROPERTY =
            "{\"transactionDates\":[{\"type\":\"BOOKING_DATE\",\"value\":{\"date\":null,\"instant\":null}}]}";

    private static final String TRX_WITHOUT_INSTANT_BOOKED_AFTER_BALANCE_SNAPSHOT =
            "{\"transactionDates\":[{\"type\":\"BOOKING_DATE\",\"value\":{\"date\":\"2000-02-01\",\"instant\":null}}]}";
    private static final String TRX_WITHOUT_LOCAL_DATE_BOOKED_AFTER_BALANCE_SNAPSHOT =
            "{\"transactionDates\":[{\"type\":\"BOOKING_DATE\",\"value\":{\"date\":null,\"instant\":\"2000-01-31T10:00:00.000Z\"}}]}";
    private static final String TRX_BOOKED_AFTER_BALANCE_SNAPSHOT =
            "{\"transactionDates\":[{\"type\":\"BOOKING_DATE\",\"value\":{\"date\":\"2000-01-31\",\"instant\":\"2000-01-31T10:00:00.000Z\"}}]}";

    private static final String TRX_WITHOUT_INSTANT_BOOKED_BEFORE_BALANCE_SNAPSHOT =
            "{\"transactionDates\":[{\"type\":\"BOOKING_DATE\",\"value\":{\"date\":\"2000-01-29\",\"instant\":null}}]}";
    private static final String TRX_BOOKED_BEFORE_BALANCE_SNAPSHOT =
            "{\"transactionDates\":[{\"type\":\"BOOKING_DATE\",\"value\":{\"date\":\"2000-01-29\",\"instant\":\"2000-01-29T10:00:00.000Z\"}}]}";

    private static final Instant BALANCE_SNAPSHOT_INSTANT =
            LocalDate.of(2000, 1, 31).atStartOfDay().atOffset(ZoneOffset.UTC).toInstant();

    @Test
    @Parameters(method = "transactionBookedAfterBalanceSnapshot")
    public void shouldReturnTrue(Transaction transaction) {
        // when
        boolean result =
                TransactionBookingDateComparator.isTransactionBookingDateAfter(
                        BALANCE_SNAPSHOT_INSTANT, transaction);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @Parameters(method = "transactionBookedBeforeBalanceSnapshotOrHasMissingBookedDate")
    public void shouldReturnFalse(Transaction transaction) {
        // when
        boolean result =
                TransactionBookingDateComparator.isTransactionBookingDateAfter(
                        BALANCE_SNAPSHOT_INSTANT, transaction);

        // then
        assertThat(result).isFalse();
    }

    private Object[] transactionBookedAfterBalanceSnapshot() {
        return new Object[] {
            SerializationUtils.deserializeFromString(
                    TRX_WITHOUT_INSTANT_BOOKED_AFTER_BALANCE_SNAPSHOT, Transaction.class),
            SerializationUtils.deserializeFromString(
                    TRX_WITHOUT_LOCAL_DATE_BOOKED_AFTER_BALANCE_SNAPSHOT, Transaction.class),
            SerializationUtils.deserializeFromString(
                    TRX_BOOKED_AFTER_BALANCE_SNAPSHOT, Transaction.class)
        };
    }

    private Object[] transactionBookedBeforeBalanceSnapshotOrHasMissingBookedDate() {
        return new Object[] {
            SerializationUtils.deserializeFromString(
                    TRX_WITHOUT_INSTANT_BOOKED_BEFORE_BALANCE_SNAPSHOT, Transaction.class),
            SerializationUtils.deserializeFromString(
                    TRX_BOOKED_BEFORE_BALANCE_SNAPSHOT, Transaction.class),
            SerializationUtils.deserializeFromString(TRX_WITHOUT_DATES, Transaction.class),
            SerializationUtils.deserializeFromString(
                    TRX_WITH_EMPTY_BOOKING_DATE_PROPERTY, Transaction.class)
        };
    }
}
