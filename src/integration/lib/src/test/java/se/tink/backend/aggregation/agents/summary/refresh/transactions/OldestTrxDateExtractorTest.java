package se.tink.backend.aggregation.agents.summary.refresh.transactions;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionDate;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.libraries.chrono.AvailableDateInformation;

public class OldestTrxDateExtractorTest {

    @Test
    public void shouldReturn20210101() {
        // given
        List<TransactionDate> dates1 =
                Arrays.asList(
                        getExecutionDate20210101(),
                        getValueDate20210102(),
                        getBookingDate20210103());
        Transaction trx1 = new Transaction();
        trx1.setTransactionDates(dates1);

        List<TransactionDate> dates2 = Collections.singletonList(getBookingDate20210104());
        Transaction trx2 = new Transaction();
        trx2.setTransactionDates(dates2);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        Transaction trx3 = new Transaction();
        trx3.setDate(new Date(calendar.getTimeInMillis()));

        List<Transaction> transactions = Arrays.asList(trx1, trx2, trx3);

        // when
        Optional<LocalDate> oldestDate = OldestTrxDateProvider.getDate(transactions);

        // then
        assertThat(oldestDate.get()).isEqualTo(LocalDate.of(2021, 1, 1));
    }

    @Test
    public void shouldReturn20201231() {
        // given
        List<TransactionDate> dates1 =
                Arrays.asList(
                        getExecutionDate20210101(),
                        getValueDate20210102(),
                        getBookingDate20210103());
        Transaction trx1 = new Transaction();
        trx1.setTransactionDates(dates1);

        List<TransactionDate> dates2 = Collections.singletonList(getBookingDate20201231());
        Transaction trx2 = new Transaction();
        trx2.setTransactionDates(dates2);

        Transaction trx3 = new Transaction();
        trx3.setDate(getDate20210105());

        List<Transaction> transactions = Arrays.asList(trx1, trx2, trx3);

        // when
        Optional<LocalDate> oldestDate = OldestTrxDateProvider.getDate(transactions);

        // then
        assertThat(oldestDate.get()).isEqualTo(LocalDate.of(2020, 12, 31));
    }

    @Test
    public void shouldReturn20201230() {
        // given
        List<TransactionDate> dates1 =
                Arrays.asList(
                        getExecutionDate20210101(),
                        getValueDate20210102(),
                        getBookingDate20210103());
        Transaction trx1 = new Transaction();
        trx1.setTransactionDates(dates1);

        Transaction trx2 = new Transaction();
        trx2.setTimestamp(LocalDate.of(2020, 12, 30).toEpochDay());

        Transaction trx3 = new Transaction();
        trx3.setDate(getDate20210105());

        List<Transaction> transactions = Arrays.asList(trx1, trx2, trx3);

        // when
        Optional<LocalDate> oldestDate = OldestTrxDateProvider.getDate(transactions);

        // then
        assertThat(oldestDate.get()).isEqualTo(LocalDate.of(2020, 12, 30));
    }

    private Date getDate20210105() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        return new Date(calendar.getTimeInMillis());
    }

    private TransactionDate getExecutionDate20210101() {
        TransactionDate date = new TransactionDate();
        AvailableDateInformation information = new AvailableDateInformation();
        information.setDate(LocalDate.of(2021, 1, 1));
        date.setValue(information);
        date.setType(TransactionDateType.EXECUTION_DATE);
        return date;
    }

    private TransactionDate getValueDate20210102() {
        TransactionDate date1 = new TransactionDate();
        AvailableDateInformation information = new AvailableDateInformation();
        information.setDate(LocalDate.of(2021, 1, 2));
        date1.setValue(information);
        date1.setType(TransactionDateType.VALUE_DATE);
        return date1;
    }

    private TransactionDate getBookingDate20210103() {
        TransactionDate date1 = new TransactionDate();
        AvailableDateInformation information = new AvailableDateInformation();
        information.setDate(LocalDate.of(2021, 1, 3));
        date1.setValue(information);
        date1.setType(TransactionDateType.BOOKING_DATE);
        return date1;
    }

    private TransactionDate getBookingDate20210104() {
        TransactionDate date1 = new TransactionDate();
        AvailableDateInformation information = new AvailableDateInformation();
        information.setDate(LocalDate.of(2021, 1, 3));
        date1.setValue(information);
        date1.setType(TransactionDateType.BOOKING_DATE);
        return date1;
    }

    private TransactionDate getBookingDate20201231() {
        TransactionDate date1 = new TransactionDate();
        AvailableDateInformation information = new AvailableDateInformation();
        information.setDate(LocalDate.of(2020, 12, 31));
        date1.setValue(information);
        date1.setType(TransactionDateType.BOOKING_DATE);
        return date1;
    }
}
