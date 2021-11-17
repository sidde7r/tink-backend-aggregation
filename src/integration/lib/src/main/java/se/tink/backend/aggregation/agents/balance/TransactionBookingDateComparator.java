package se.tink.backend.aggregation.agents.balance;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.function.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.models.Transaction;
import se.tink.backend.aggregation.agents.models.TransactionDateType;
import se.tink.libraries.chrono.AvailableDateInformation;

public class TransactionBookingDateComparator {

    private static final Logger log =
            LoggerFactory.getLogger(TransactionBookingDateComparator.class);

    private static final String SKIP_TRANSACTION_MSG =
            "Skipping transaction due to missing booking date!";

    public static Predicate<Transaction> isTransactionBookingDateAfter(Instant instant) {
        return trx -> isTransactionBookingDateAfter(instant, trx);
    }

    public static boolean isTransactionBookingDateAfter(Instant instant, Transaction transaction) {
        Optional<AvailableDateInformation> dateInformationOptional =
                transaction.getAvailableDateInformationOf(TransactionDateType.BOOKING_DATE);

        if (!dateInformationOptional.isPresent()) {
            log.warn("[TRANSACTION BOOKING DATE COMPARATOR] {}", SKIP_TRANSACTION_MSG);
            return false;
        }

        AvailableDateInformation dateInformation = dateInformationOptional.get();
        Instant transactionBookingInstant = dateInformation.getInstant();
        if (transactionBookingInstant != null) {
            return transactionBookingInstant.isAfter(instant);
        }

        LocalDate bookingDate = dateInformation.getDate();
        if (bookingDate != null) {
            return toInstantStartOfDayUTC(bookingDate).isAfter(instant);
        }

        log.warn("[TRANSACTION BOOKING DATE COMPARATOR] {}", SKIP_TRANSACTION_MSG);
        return false;
    }

    private static Instant toInstantStartOfDayUTC(LocalDate bookingDate) {
        log.info(
                "[TRANSACTION BOOKING DATE COMPARATOR] Converting booking date to instant by adding start of the day time in UTC");
        return bookingDate.atStartOfDay().atZone(ZoneOffset.UTC).toInstant();
    }
}
